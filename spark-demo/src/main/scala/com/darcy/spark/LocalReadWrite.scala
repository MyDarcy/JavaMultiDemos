package com.darcy.spark

import java.io.File
import java.net.URI
import java.nio.file.{Files, Paths}

import org.apache.spark.sql.SparkSession

/**
  * 1. 启动hdfs
  * 2. 本地文件读写要加 file://
  * https://blog.csdn.net/helloxiaozhe/article/details/78480108
  */
object LocalReadWrite {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[4]")
      .appName("spark-local-read-write") //
      //            .config("spark.sql.warehouse.dir",warehouseLocation)
      .enableHiveSupport()
      .getOrCreate()

    val fileName = "file:///Users/darcy/IdeaProjects/JavaMultiDemos/spark-demo/doc/numbers.txt"
//    val rdd = spark.sparkContext.textFile(fileName)
    val rdd = spark.sparkContext.parallelize(1 to 10, 4);
    // save到了一个文件夹里面; 需要保存到单个文件中;
    val prefix = "file://"
    val fileDir = "/Users/darcy/IdeaProjects/JavaMultiDemos/spark-demo/doc/numbers-duplication10.txt"
    rdd.coalesce(1, true).saveAsTextFile(prefix + fileDir)
    rdd.foreach(println)

    println("Files.")
    import scala.collection.JavaConversions;
    val hdfsLocalFile = fileDir + "/" + "part-00000"
    println(s"hdfsLocalFile:${hdfsLocalFile}")


  }

}
