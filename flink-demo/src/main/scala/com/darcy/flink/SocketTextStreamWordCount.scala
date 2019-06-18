package com.darcy.flink

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.util.Collector;

final class LineSplitter extends FlatMapFunction[String, Tuple2[String, Integer]] {
  override def flatMap(s: String, collector: Collector[Tuple2[String, Integer]]): Unit = {
    val tokens = s.toLowerCase.split("\\W+")
    for (token <- tokens) {
      if (token.length > 0) collector.collect(new Tuple2[String, Integer](token, 1))
    }
  }
}

object SocketTextStreamWordCount {

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("USAGE:\nSocketTextStreamWordCount <hostname> <port>")
      return
    }

    val hostName = args(0)
    val port = args(1).toInt

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val stream = env.socketTextStream(hostName, port)

    val sum = stream.flatMap(new LineSplitter())
      .keyBy(0)
      .sum(1)

    sum.print()
    env.execute("Scala WordCount From SocketTextStream Example.")

  }
}
