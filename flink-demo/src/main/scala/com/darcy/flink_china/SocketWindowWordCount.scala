package com.darcy.flink_china


import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

/**
  * 可执行的例子：com.darcy.flink_china.local.SocketWindowWordCount
  * ifconfig
  * ip: 172.30.30.27
  *
  * First of all, we use netcat to start local server via
  * $ nc -l 9000
  *
  * Submit the Flink program:
  * $ ./bin/flink run examples/streaming/SocketWindowWordCount.jar --port 9000
  * Starting execution of program
  *
  * $ nc -l 9000
  * lorem ipsum
  * ipsum ipsum ipsum
  * bye
  *
  * $ tail -f log/flink-*-taskexecutor-*.out
  * lorem : 1
  * bye : 1
  * ipsum : 4
  *
  * $ ./bin/stop-cluster.sh
  */
object SocketWindowWordCount {

  def main(args: Array[String]) : Unit = {

    // the port to connect to
    val ipport = try {
      val tool = ParameterTool.fromArgs(args)
      val ip = tool.get("ip")
      val port = tool.getInt("port")
      (ip, port)
    } catch {
      case e: Exception => {
        System.err.println("No port specified. Please run 'SocketWindowWordCount --ip <ip> --port <port>'")
        return
      }
    }

    // get the execution environment
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // get input data by connecting to the socket
    val text = env.socketTextStream(ipport._1, ipport._2, '\n')

    // parse the data, **group it**, window it, and aggregate the counts
    val windowCounts = text
      .flatMap { w => w.split("\\s+") }
      .map { w => WordWithCount(w, 1) }
      .keyBy("word") // 分组
      .timeWindow(Time.seconds(5), Time.seconds(1)) // 每秒滑动一次，每次窗口大小5秒;
      .sum("count")

    // print the results with a single thread, rather than in parallel
    windowCounts.print().setParallelism(1) // 单线程输出;

    env.execute("Socket-Window-WordCount")
  }

  // Data type for words with count
  case class WordWithCount(word: String, count: Long)
}
