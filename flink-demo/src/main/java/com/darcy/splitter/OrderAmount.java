package com.darcy.splitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meituan.flink.common.config.KafkaTopic;
import com.meituan.flink.common.kafka.MTKafkaConsumer08;
import com.meituan.flink.common.kafka.MTKafkaProducer08;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer08;

import java.util.Map;


/**
 * QuickStart: sum order amount group by dealId.
 * You can see the topic[log.orderlog] at http://kafka.data.sankuai.com/
 */
public class OrderAmount {

    public static void main(String[] args) throws Exception {

        String jobName = OrderAmount.class.getName();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // create a kafka source
        MTKafkaConsumer08 consumer = new MTKafkaConsumer08(args);
        consumer.build(new SimpleStringSchema());
        Map.Entry<KafkaTopic, FlinkKafkaConsumerBase> consumerBaseEntry = consumer.getConsumerByName("log.orderlog", "");
        DataStream<String> stream = env.addSource(consumerBaseEntry.getValue());

        // parse the data, group it, window it, and aggregate the counts
        DataStream<Tuple2<Integer, Double>> orderAmount =
                stream.map(new ParseOrderRecord())
                .filter(new RecordFilter())
                .keyBy("dealId")
                .window(TumblingProcessingTimeWindows.of(Time.seconds(30)))
                .aggregate(new AmountSum());

        DataStream<String> result = orderAmount.map(new FormatResult());

        // add a kafka sink to a DataStream
        MTKafkaProducer08 producer = new MTKafkaProducer08(args);
        producer.build(new SimpleStringSchema());
        Map.Entry<KafkaTopic, FlinkKafkaProducer08> producer08Entry = producer.getProducerByName("app.your_own_topic");
        result.addSink(producer08Entry.getValue());

        env.execute("OrderAmount!");
    }

    public static class ParseOrderRecord implements MapFunction<String, OrderRecord> {

        @Override
        public OrderRecord map(String s) throws Exception {
            JSONObject jsonObject = JSON.parseObject(s);
            long id = jsonObject.getLong("id");
            int dealId = jsonObject.getInteger("dealid");
            String action = jsonObject.getString("_mt_action");
            double amount = jsonObject.getDouble("amount");

            return new OrderRecord(id, dealId, action, amount);
        }
    }

    public static class RecordFilter implements FilterFunction<OrderRecord> {

        @Override
        public boolean filter(OrderRecord orderRecord) throws Exception {
            return "PAY".equals(orderRecord.action);
        }
    }

    public static class AmountSum implements AggregateFunction<
            OrderRecord, // input type
            Tuple2<Integer, Double>, // acc
            Tuple2<Integer, Double>> {  // output type

        @Override
        public Tuple2<Integer, Double> createAccumulator() {
            return new Tuple2<>(0, 0.0);
        }

        @Override
        public Tuple2<Integer, Double> add(OrderRecord value, Tuple2<Integer, Double> accumulator) {
            accumulator.setField( value.dealId,0);
            accumulator.setField(accumulator.f1 + value.amount, 1);
            return accumulator;
        }

        @Override
        public Tuple2<Integer, Double> getResult(Tuple2<Integer, Double> accumulator) {
            return accumulator;
        }

        @Override
        public Tuple2<Integer, Double> merge(Tuple2<Integer, Double> a, Tuple2<Integer, Double> b) {
            return new Tuple2<>(a.f0, a.f1 + b.f1);
        }
    }

    public static class FormatResult implements MapFunction<Tuple2<Integer, Double>, String> {

        @Override
        public String map(Tuple2<Integer, Double> t) throws Exception {
            return t.getField(0) + ": " + t.getField(1);
        }
    }

    public static class OrderRecord {

        public long id;
        public int dealId;
        public String action;
        public double amount;

        public OrderRecord() {}

        public OrderRecord(long id, int dealId, String action, double amount) {
            this.id = id;
            this.dealId = dealId;
            this.action = action;
            this.amount = amount;
        }
    }
}
