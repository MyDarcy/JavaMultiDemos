package com.darcy.flink.tutorial;

import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class IterativeStreamDemo {

  public static void main(String[] args) throws IOException {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
    DataStream<Long> someIntegers = env.generateSequence(0, 1000);

    IterativeStream<Long> iteration = someIntegers.iterate();

    DataStream<Long> minusOne = iteration.map(new MapFunction<Long, Long>() {
      @Override
      public Long map(Long value) throws Exception {
        return value - 1 ;
      }
    });

    DataStream<Long> stillGreaterThanZero = minusOne.filter(new FilterFunction<Long>() {
      @Override
      public boolean filter(Long value) throws Exception {
        return (value > 0);
      }
    });

    iteration.closeWith(stillGreaterThanZero);

    DataStream<Long> lessThanZero = minusOne.filter(new FilterFunction<Long>() {
      @Override
      public boolean filter(Long value) throws Exception {
        return (value <= 0);
      }
    });

    ArrayList<Long> lists = Lists.newArrayList(DataStreamUtils.collect(stillGreaterThanZero));
    System.out.println(lists);
    System.out.println();
    lists = Lists.newArrayList(DataStreamUtils.collect(lessThanZero));
    System.out.println(lists);
    System.out.println();
    lists = Lists.newArrayList(DataStreamUtils.collect(someIntegers));
    System.out.println(lists);

  }

}
