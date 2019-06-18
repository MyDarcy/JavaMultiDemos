package com.darcy.splitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.meituan.data.binlog.BinlogColumn;
import com.meituan.data.binlog.BinlogEntry;
import com.meituan.data.binlog.BinlogEntryUtil;
import com.meituan.data.binlog.BinlogRow;
import com.meituan.flink.common.config.KafkaTopic;
import com.meituan.flink.common.kafka.MTKafkaConsumer08;
import com.meituan.flink.common.kafka.MTKafkaProducer08;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer08;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.Map;

/**
 * Created by lds on 2017/5/10.
 */
public class ParseBinlog {
    public static void main(String[] args) throws Exception {
//        String jobName = FlinkConf.getJobName(args);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // create a kafka source
        MTKafkaConsumer08 consumer = new MTKafkaConsumer08(args);
        Map.Entry<KafkaTopic, FlinkKafkaConsumerBase> consumerBaseEntry = consumer.getConsumerByName("binlog.order_binlog", "");
        DataStream<BinlogEntry> stream = env.addSource(consumerBaseEntry.getValue());

        // parse the data
        DataStream<String> orderStream =
                stream.flatMap(new BinlogParse());

        // add a kafka sink to a DataStream
        MTKafkaProducer08 producer = new MTKafkaProducer08(args);
        Map.Entry<KafkaTopic, FlinkKafkaProducer08> producer08Entry = producer.getProducerByName("app.your_own_topic");
        orderStream.addSink(producer08Entry.getValue());

        env.execute("TestBinlog!");
    }



    private static class BinlogParse implements FlatMapFunction<BinlogEntry, String> {

        @Override
        public void flatMap(BinlogEntry value, Collector<String> out) throws Exception {
            long executeTime = value.getExecuteTime();
            String tableName = value.getTableName();
            String eventType = value.getEventType();

            if (tableName.matches("order_dealid_.*")) {
                for (BinlogRow row : value.getRowDatas()) {
                    OrderRecord orderRecord = new OrderRecord(executeTime, tableName, eventType);
                    Map<String, BinlogColumn> cur = row.getCurColumns();
                    Map<String, BinlogColumn> before = null;
                    if (eventType.equals(BinlogRow.EVENT_TYPE_UPDATE)) {
                        before = row.getBeforeColumns();
                    }

                    OrderFields curColumns = new OrderFields();
                    curColumns.setId(cur.get("id").getValue());
                    curColumns.setUserid(cur.get("userid").getValue());
                    curColumns.setOrdertime(cur.get("ordertime").getValue());
                    curColumns.setPaytime(cur.get("paytime").getValue());
                    curColumns.setAmount(cur.get("amount").getValue());
                    curColumns.setStatus(cur.get("status").getValue());
                    orderRecord.setCurColumns(curColumns);

                    if (before != null) {
                        OrderFields beforeColumns = new OrderFields();
                        beforeColumns.setPaytime(before.get("paytime").getValue());
                        beforeColumns.setStatus(before.get("status").getValue());
                        orderRecord.setBeforeColumns(beforeColumns);
                    } else {
                        orderRecord.setBeforeColumns(new OrderFields());
                    }

                    out.collect(JSON.toJSONString(orderRecord));
                }
            }
        }
    }

    public static class OrderRecord {

        public long executeTime;
        public String tableName;
        public String eventType;
        public OrderFields beforeColumns;
        public OrderFields curColumns;

        public OrderRecord(long executeTime, String tableName, String eventType) {
            this.executeTime = executeTime;
            this.tableName = tableName;
            this.eventType = eventType;
        }

        public long getExecuteTime() {
            return executeTime;
        }

        public void setExecuteTime(long executeTime) {
            this.executeTime = executeTime;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public OrderFields getBeforeColumns() {
            return beforeColumns;
        }

        public void setBeforeColumns(OrderFields beforeColumns) {
            this.beforeColumns = beforeColumns;
        }

        public OrderFields getCurColumns() {
            return curColumns;
        }

        public void setCurColumns(OrderFields curColumns) {
            this.curColumns = curColumns;
        }
    }

    public static class OrderFields {

        public String id;
        public String userid;
        public String ordertime;
        public String paytime;
        public String amount;
        public String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getOrdertime() {
            return ordertime;
        }

        public void setOrdertime(String ordertime) {
            this.ordertime = ordertime;
        }

        public String getPaytime() {
            return paytime;
        }

        public void setPaytime(String paytime) {
            this.paytime = paytime;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private static class RawSchema extends AbstractDeserializationSchema<BinlogEntry> {

        @Override
        public BinlogEntry deserialize(byte[] message) throws IOException {
            Entry entry = BinlogEntryUtil.deserializeFromProtoBuf(message);
            BinlogEntry binlogEntry = BinlogEntryUtil.serializeToBean(entry);

            return binlogEntry;
        }
    }

}
