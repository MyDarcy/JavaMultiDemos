package com.darcy.window.session;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * https://km.sankuai.com/page/117368859
 * 我需要计算出ip在不同分组、不同机器下每分钟请求数量，以此评估打到每台机器的流量是否均匀，难点在于不同ip在不同机器上出现请求并不是整时整点，如下图，ip生命周期是16:13:21~16:17:53，ip在16:13:21~16:14:00时间端的请求量并不能代表16点13分钟整体请求量，同样16:17:00~16:17:53的请求量也不能代表16点17分钟的请求量；整体设计需要有ip生命周期的概念，Session Window是一个会话窗口，适合这样的数据场景
 *
 * （1）EventTime + Watermark指定使用数据上的时间
 * （2）keyBy进行分组，也就是Session Window里的会话对象
 * （3）EventTimeSessionWindows.withGap设置一个session window的gap，在同一个session window中的数据表示ip的生命周期，超过gap认为生命结束
 */
public class FasIpRangeStreamingJob {

  public static void main(String[] args) throws Exception {

    final String jobName = FasIpRangeStreamingJob.class.getName();

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // 设置使用 EventTime 作为时间戳(默认是 ProcessingTime)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

    env.setParallelism(1);

    DataStream<String> stream = env.socketTextStream("localhost", 9000, "\n");

    DataStream<FlowIpRangeOutputFormat> handleStream = stream.map(new FlowIpRangeParseMap())
        .filter(new FlowKeyNotNullFilter())
        .assignTimestampsAndWatermarks(new BoundedOutOfRangeGenerator())
        .keyBy("group_key", "proxy_ip", "domain", "instance_id")
        .window(EventTimeSessionWindows.withGap(Time.minutes(5)))
        .allowedLateness(Time.minutes(1))
        .trigger(ContinuousEventTimeTrigger.of(Time.minutes(1))) // 自定义触发机制; 原始触发器只能实现部分简单效果，无法满足对应特例场景的业务需求，EventTimeTrigger在gap产生时触发，ContinuousEventTimeTrigger在指定间隔时间段触发
        .aggregate(new FlowIpRangeAggregate(), new FlowIpRangeWindow()); // apply指定一个WindowFunction，拿到的是一个Iterable迭代器，他会缓存这一窗口的所有数据，那么在窗口间隔大，数据量大的情况下，内存是否扛得住就是问题了，我们可以通过增量窗口聚合方式进行解决

    DataStream<JSONObject> result = handleStream.map(new FlowIpRangeFormatMap());

    result.print();
    env.execute(jobName);

  }
}

