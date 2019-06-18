package com.darcy.flink.tutorial;

import org.apache.flink.api.common.accumulators.LongCounter;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditEvent;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditsSource;

public class WikipediaAnalysis {

  public static void main(String[] args) throws Exception {
    // create a StreamExecutionEnvironment; This can be used to set execution parameters and create sources for reading from external systems.
    StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();

    // create a source that reads from the Wikipedia IRC log
    DataStreamSource<WikipediaEditEvent> source = see.addSource(new WikipediaEditsSource());

    KeyedStream<WikipediaEditEvent, String> keyedEditStream = source.keyBy(new KeySelector<WikipediaEditEvent, String>() {
      @Override
      public String getKey(WikipediaEditEvent value) throws Exception {
        return value.getUser();
      }
    });

    SingleOutputStreamOperator<PairCounter> result = keyedEditStream
        .timeWindow(Time.seconds(5))
        .aggregate(new AggregateFunction<WikipediaEditEvent, PairCounter, PairCounter>() {
          @Override
          public PairCounter createAccumulator() {
            PairCounter pairCounter = new PairCounter();
            pairCounter.bytes = 0L;
            return pairCounter;
          }

          @Override
          public PairCounter add(WikipediaEditEvent value, PairCounter accumulator) {
            accumulator.user = value.getUser();
            accumulator.bytes += value.getByteDiff();
            return accumulator;
          }

          @Override
          public PairCounter getResult(PairCounter accumulator) {
            return accumulator;
          }

          @Override
          public PairCounter merge(PairCounter a, PairCounter b) {
            a.bytes += b.bytes;
            return a;
          }
        });

    result.print();
    see.execute("JOB");
  }

  static class PairCounter {
    public String user;
    public Long bytes;

    @Override
    public String toString() {
      return "user:" + user + ", bytes:" + bytes;
    }
  }

}
