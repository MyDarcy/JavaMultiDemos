package com.darcy.spark

import java.io.File
import java.nio.file.Files

import org.apache.spark.sql.SparkSession

import scala.util.Random

object LocalHdfsReadWrite {

  def main(args: Array[String]): Unit = {
    saveHdfsFile()
  }

  def saveHdfsFile()= {
    val spark = SparkSession.builder()
      .master("local[4]")
      .appName("spark-local-hdfs-read-write") //
      //            .config("spark.sql.warehouse.dir",warehouseLocation)
      .enableHiveSupport()
      .getOrCreate()

    val localFs = "/Users/darcy/IdeaProjects/JavaMultiDemos/spark-demo/doc/"
    val localHdoopDir = "hdfs:///user/darcy/test/2019/12/13"
    val rdd = spark.sparkContext.parallelize(1 to 10, 4).map(_.toString)
    val hadoopPath = localHdoopDir + "lbs_2012"
    HdfsUtils.saveDataToHDFS(spark.sparkContext, hadoopPath, rdd)
    var exists = HdfsUtils.existsDir(spark.sparkContext, hadoopPath)
    println(exists)
    HdfsUtils.deleteDir(spark.sparkContext, hadoopPath)
    exists = HdfsUtils.existsDir(spark.sparkContext, hadoopPath)
    println(exists)
  }

  def saveAsSingleFile()= {
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
    import com.darcy.spark.SparkHelper.RDDExtensions
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
