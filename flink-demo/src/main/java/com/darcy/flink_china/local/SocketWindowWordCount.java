package com.darcy.flink_china.local;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * 执行步骤见 object SocketWindowWordCount or SocketTextStreamWordCount(验证可行)
 * 1. nc -l 172.30.30.27  9000
 * 2. $ flink run -c com.darcy.flink_china.local.SocketWindowWordCount flink-demo.jar --ip 172.30.30.27  --port 9000
 *    Starting execution of program
 *
 * 3. # darcy @ darcy in ~/opt/flink-1.8.0-scala-2.11/log [10:36:54]
 * $ tail -f flink-*-taskexecutor-*.out
 *
 * ==> flink-darcy-taskexecutor-0-darcy.local.out <==
 * SocketWindowWordCount.WordWithCount(word=class, count=5)
 * SocketWindowWordCount.WordWithCount(word=public, count=3)
 * SocketWindowWordCount.WordWithCount(word=SocketWindowWordCount, count=3)
 * SocketWindowWordCount.WordWithCount(word=class, count=3)
 * ...
 * SocketWindowWordCount.WordWithCount(word=String, count=2)
 * SocketWindowWordCount.WordWithCount(word=, count=27)
 * SocketWindowWordCount.WordWithCount(word=ip;, count=2)
 * SocketWindowWordCount.WordWithCount(word=port;, count=2)
 * SocketWindowWordCount.WordWithCount(word=try, count=3)
 * SocketWindowWordCount.WordWithCount(word=final, count=2)
 * SocketWindowWordCount.WordWithCount(word=port;, count=1)
 * SocketWindowWordCount.WordWithCount(word=try, count=1)
 * SocketWindowWordCount.WordWithCount(word={, count=1)
 */
public class SocketWindowWordCount {

  public static void main(String[] args) throws Exception {

// the port to connect to
    final int port;
    final String ip;
    try {
      final ParameterTool params = ParameterTool.fromArgs(args);
      port = params.getInt("port");
      ip = params.get("ip");
      System.out.println("ip:" + ip + ", port:" + port);
    } catch (Exception e) {
      System.err.println("No port specified. Please run 'SocketWindowWordCount --port <port>'");
      return;
    }

// get the execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

// get input data by connecting to the socket
    DataStream<String> text = env.socketTextStream(ip, port, "\n");

// parse the data, group it, window it, and aggregate the counts
    DataStream<WordWithCount> windowCounts = text
        .flatMap(new FlatMapFunction<String, WordWithCount>() {
          @Override
          public void flatMap(String value, Collector<WordWithCount> out) {
            for (String word : value.split("\\s")) {
              out.collect(new WordWithCount(word, 1L));
            }
          }
        })
        .keyBy("word")
        .timeWindow(Time.seconds(5), Time.seconds(1))
        .reduce(new ReduceFunction<WordWithCount>() {
          @Override
          public WordWithCount reduce(WordWithCount a, WordWithCount b) {
            return new WordWithCount(a.word, a.count + b.count);
          }
        });

// print the results with a single thread, rather than in parallel
    windowCounts.print().setParallelism(1);

    env.execute("Socket Window WordCount");
  }

  // Data type for words with count
  @Data
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WordWithCount {

    public String word;
    public long count;


  }
}
