package com.darcy.flink.tutorial;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class BatchProcess {

  public static void main(String[] args) throws Exception {
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    DataSet<String> text = env.readTextFile("/Users/darcy/IdeaProjects/multidemos/flink-demo/src/main/java/com/darcy/flink/tutorial/BatchProcess.java");

    DataSet<Tuple2<String, Integer>> counts =
        // split up the lines in pairs (2-tuples) containing: (word,1)
        text.flatMap(new Tokenizer())
            // group by the tuple field "0" and sum up tuple field "1"
            .groupBy(0)
            .sum(1);

    counts.print();
    counts.writeAsCsv("/Users/darcy/IdeaProjects/multidemos/doc/count.dat", "\n", " ");

  }

  static class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

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
