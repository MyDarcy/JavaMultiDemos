package com.darcy.window;

import com.meituan.flink.common.config.KafkaTopic;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer08;

import com.alibaba.fastjson.JSON;
import com.meituan.flink.common.kafka.MTKafkaConsumer08;
import com.meituan.flink.common.kafka.MTKafkaProducer08;

import java.io.IOException;
import java.util.Map;

/*
https://km.sankuai.com/page/28406620

{
    "extend_two":"null",
    "phoneType":"PC",
    "pushTime":"1511145578730",
    "extend_three":"null",
    "pushId":"0",
    "_mt_datetime":"2017-11-20 10:39:38",
    "_mt_millisecond":"730",
    "msgId":"6338203945444046696",
    "_mt_level":"10.32.165.225",
    "_mt_logger_name":"",
    "pushName":"PC",
    "_mt_thread":"waimai_push_trace",
    "op_content":"配送通知推送到PC",
    "orderId":"7180065202",
    "status_code":"3",
    "op_code":"12",
    "_mt_servername":"INFO",
    "status_content":"调用push-client推送失败!!",
    "_mt_appkey":"dx-waimai-e-messagecenter04.dx.sankuai.com",
    "pushType":"3",
    "message":"push trace log",
    "pushToken":"0nOtbYMysJWCIuE5UJT0YMF3jmpFSU9QkKt081db9tKc*",
    "netType":"null",
    "extend_one":"从KV获取host失败,该pushToken不在线",
    "pushSdk":"null",
    "wmPoiId":"391645",
    "appKey":"waimai_e"
}
 */
public class SlideTimeWindowJob {

  public static void main(String[] args) throws Exception {
    ParameterTool params = ParameterTool.fromArgs(args);
    String jobName = params.getRequired("__jobName");
    String nameSpace = params.getRequired("__nameSpace");

    // #0 配置执行环境
    final StreamExecutionEnvironment env = getExecutionEnvironment(params);

    env.disableOperatorChaining(); //禁止flink的算子链优化，方便新手查看作业的可视化结构图。 ！！！线上生产时需要注释掉，这个优化吞吐量非常明显

    // #1 配置输入源
    String sourceTopic = "log.waimai_push_trace";
    MTKafkaConsumer08 consumer = new MTKafkaConsumer08(args);
    consumer.build(new SimpleStringSchema());
    Map.Entry<KafkaTopic, FlinkKafkaConsumerBase> consumerEntry = consumer.getConsumerByName(sourceTopic, nameSpace);
    String kafkaStartOffset = params.get("kafkaStartOffset", "");
    if (kafkaStartOffset.equals("")) {
      consumerEntry.getValue().setStartFromLatest();   //接着上一次开始消费
    } else {
      consumerEntry.getValue().setStartFromEarliest();  //从头开始消费
    }

    DataStream<String> input = env.addSource(consumerEntry.getValue()).name("source kafka topic:" + sourceTopic);

    // #2 业务代码
    Time maxOutOfOrderness = Time.seconds(5);
    SingleOutputStreamOperator<String> result = input
        .map(x -> JSON.parseObject(x, PushLogMsg.class))    //  String  ->   PushLogMsg Object
        .map(x -> new Tuple3<Integer, Long, Long>(x.wmPoiId, x.pushTime, 1L))    // PushLogMsg Object  -> Tuple3,  扔掉不必要的字段减少网络IO消耗
        .assignTimestampsAndWatermarks(new DataAssignWatermark(maxOutOfOrderness))
        .keyBy(0)  //group by wmPoiId
        .window(SlidingEventTimeWindows.of(Time.minutes(10), Time.minutes(2)))  // 窗口每2分钟滑动一次，窗口的宽度是10分钟的数据。
        .sum(2)  // sum tuple3.f2
        .name("sliding event time window")
        .map(x -> "wmPoiId=" + x.f0 + ",count=" + x.f2);   // Tuple3  -> String, 目前写入Kafka只支持String类型


    // #3 配置输出汇
    String sinkTopic = "test.zh_flink_window";
    MTKafkaProducer08 producer = new MTKafkaProducer08(args);
    Map.Entry<KafkaTopic, FlinkKafkaProducer08> producerEntry = producer.getProducerByName(sinkTopic);
    result.addSink(producerEntry.getValue()).name("sink kafka topic:" + sinkTopic);

    System.out.println(env.getExecutionPlan());

    // execute program
    env.execute(jobName);
  }


  /**
   * 获取流处理的执行环境以及配置相关参数
   *
   * @param params
   * @return
   * @throws IOException
   */
  private static StreamExecutionEnvironment getExecutionEnvironment(ParameterTool params) throws IOException {
    //获取流处理的执行环境
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    //设置重启策略
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(1000, 1000));
    //设置每6秒触发一次检查点
    env.enableCheckpointing(6000, CheckpointingMode.EXACTLY_ONCE);

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    //设置job的全局参数
    env.getConfig().setGlobalJobParameters(params);
    return env;
  }

  /**
   * Watermark生成规则
   */
  private static class DataAssignWatermark extends BoundedOutOfOrdernessTimestampExtractor<Tuple3<Integer, Long, Long>> {

    public DataAssignWatermark(Time maxOutOfOrderness) {
      super(maxOutOfOrderness);
    }

    @Override
    public long extractTimestamp(Tuple3<Integer, Long, Long> element) {
      return element.f1;
    }
  }


  @Data
  @NoArgsConstructor
  static class PushLogMsg {

    // !!! All fields are either public or must be accessible through getter and setter functions. !!!
    public String appKey;

    public String mtServerName;

    public String extendOne;

    public Long msgId;

    public String netType;

    public Integer opCode;

    public String opContent;

    public Long orderId;

    public String phoneType;

    public Integer pushId;

    public String pushName;

    public String pushSdk;

    public Long pushTime;

    public String pushToken;

    public Integer pushType;

    public Integer statusCode;

    public String statusContent;

    public Integer wmPoiId;

    public static PushLogMsg fromString(String s) {
      return JSON.parseObject(s, PushLogMsg.class);
    }

  }
}

