package com.darcy.splitter;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * https://km.sankuai.com/page/28406626 及其 demo工程;
 *
 * Implements the "WordCount" program that computes a simple word occurrence histogram
 * over some sample data
 *
 * <p>
 * This example shows how to:
 * <ul>
 * <li>write a simple Flink program.
 * <li>use Tuple data types.
 * <li>write and use user-defined functions.
 * </ul>
 *
 * 可以运行
 * 1.将 pom.xml 中 flink-streaming 的依赖的 <scope>provided</scope> 注释掉
 * 2.工程目录执行 sub-project 所在的目录: mvn compile exec:java -Dexec.mainClass=XXX
 *
 *
 */
public class WordCountStreaming {

  private static final Logger logger = LoggerFactory.getLogger(WordCountStreaming.class);

  public static void main(String[] args) throws Exception {
    // 固定 jobName 的方式仅为方便本地调试
    // 线上作业请使用 FlinkConf.getJobName(args) 作为 jobName！请不要自行指定！
    String jobName = "WordCountStreaming";
    // String jobName = FlinkConf.getJobName(args);

    // set up the execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // get input data
    DataStream<String> text = env.fromElements(
        "To be, or not to be,--that is the question:--",
        "Whether 'tis nobler in the mind to suffer",
        "The slings and arrows of outrageous fortune",
        "Or to take arms against a sea of troubles,"
    ).setParallelism(1);

    DataStream<Tuple2<String, Integer>> counts =
        // split up the lines in pairs (2-tuples) containing: (word,1)
        text.flatMap(new LineSplitter()).setParallelism(1)
            // group by the tuple field "0" and sum up tuple field "1"
            .keyBy(0)
            .sum(1)
            .setParallelism(1);

    // execute and print result
    counts.print().setParallelism(1);

    env.execute(jobName);
  }

  /**
   * Implements the string tokenizer that splits sentences into words as a user-defined
   * FlatMapFunction. The function takes a line (String) and splits it into
   * multiple pairs in the form of "(word,1)" (Tuple2<String, Integer>).
   */
  public static final class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {

    @Override
    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
      // normalize and split the line
      String[] tokens = value.toLowerCase().split("\\W+");
      // emit the pairs
      for (String token : tokens) {
        if (token.length() > 0) {
          out.collect(new Tuple2<String, Integer>(token, 1));
        }
      }
    }
  }
}


