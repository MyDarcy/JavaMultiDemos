package com.darcy.spark

import java.io.File
import java.nio.file.Files

import com.darcy.spark.SparkHelper.RDDExtensions
import org.apache.spark.sql.SparkSession

object LocalHdfsReadWrite {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[4]")
      .appName("spark-local-hdfs-read-write") //
      //            .config("spark.sql.warehouse.dir",warehouseLocation)
      .enableHiveSupport()
      .getOrCreate()

    val localFs = "/Users/darcy/IdeaProjects/JavaMultiDemos/spark-demo/doc/"
    val localHdoopDir = "hdfs:///user/darcy/test/"
    val rdd = spark.sparkContext.parallelize(1 to 10, 4).map(_.toString)
    val hadoopPath = localHdoopDir + "20191017/file-2"
    println(hadoopPath)
    val localFile = localFs + "/file-2"
    println(localFile)
    val file = new File(localFile)
    if (!file.exists()) {
      file.createNewFile()
    }
    rdd.saveAsSingleTextFile(hadoopPath)
    HdfsUtils.hdfsFileCopytoLocal(hadoopPath, localFile, spark.sparkContext)
    println("read local File.")
    import scala.collection.JavaConverters._
    Files.readAllLines(file.toPath).asScala.foreach(i => println(i))
    if (file.exists()) {
      file.delete()
    }
  }

}
