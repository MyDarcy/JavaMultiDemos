package com.darcy.watermark

import java.text.SimpleDateFormat

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

/**
  * https://blog.csdn.net/lmalds/article/details/52704170
  */
object WatermarkDemo {

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      System.err.println("USAGE:\nWatermarkDemo <hostname> <port>")
      return
    }

    val hostName = args(0)
    val port = args(1).toInt

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val input = env.socketTextStream(hostName, port)

    val inputMap = input.map(f => {
      val arr = f.split("\\W+")
      val code = arr(0)
      val time = arr(1).toLong
      (code, time)
    })

    val watermark = inputMap.assignTimestampsAndWatermarks(new TimestampExtractor)

    val window = watermark
      .keyBy(_._1)
      .window(TumblingEventTimeWindows.of(Time.seconds(3)))
      .apply(new WindowFunctionTest)

    window.print()

    env.execute("watermark-demo");
  }

  class WindowFunctionTest extends WindowFunction[(String, Long), (String, Int, String, String, String, String), String, TimeWindow] {

    override def apply(key: String, window: TimeWindow, input: Iterable[(String, Long)], out: Collector[(String, Int, String, String, String, String)]): Unit = {
      val list = input.toList.sortBy(_._2)
      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
      out.collect(key, input.size, format.format(list.head._2), format.format(list.last._2), format.format(window.getStart), format.format(window.getEnd))
    }

  }

  class TimestampExtractor extends AssignerWithPeriodicWatermarks[(String, Long)] {

    var currentMaxTimestamp = 0L
    val maxOutOfOrderness = 10000L //最大允许的乱序时间是10s

    var watermark: Watermark = null

    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    override def getCurrentWatermark: Watermark = {
      return new Watermark(currentMaxTimestamp - maxOutOfOrderness)
    }

    override def extractTimestamp(t: (String, Long), l: Long): Long = {
      val timestamp = t._2
      currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp)
      println("record:" + t._1 + ",\t" + t._2 + "|" + format.format(t._2) + ",\t" + currentMaxTimestamp + "|" + format.format(currentMaxTimestamp) + ",\t" + watermark.toString)
      timestamp
    }
  }

}
