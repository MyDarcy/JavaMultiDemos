package com.darcy.spark

import org.apache.spark.sql.SparkSession

/**
  *
  * @author hezhiqiang05
  * @version 1.0
  */
object SparkHiveReadWrite {

  private val  path:String = "/user/darcy/iteblog.json"

  def main(args: Array[String]): Unit = {
    val warehouseLocation = "hdfs://localhost:9000/user/hive122/warehouse"
    val localWarehouselocation = "file:///Users/darcy/IdeaProjects/multidemos/spark-warehouse"
    val spark = SparkSession.builder()
            .master("local")
            .appName("spark-hive-read-write") //
//            .config("spark.sql.warehouse.dir",warehouseLocation)
            .enableHiveSupport()
            .getOrCreate()

    spark.catalog.listDatabases().show(false)
    spark.catalog.listTables("mydb").show(false)
    spark.conf.getAll.mkString("\n")

    dataSetDemo(spark)
    dataframeDemo(spark)
    jsonDemo(spark)

  }

  def dataSetDemo(spark: SparkSession): Unit = {
    val numberDataset = spark.range(5, 500, 5)
    numberDataset.show(5)
  }

  def dataframeDemo(spark: SparkSession) = {
    val langPercentDataFrame = spark.createDataFrame(List(
      ("scala", 25),
      ("python", 30),
      ("java", 30),
      ("go", 15)
    ))

    val renamedResult = langPercentDataFrame.withColumnRenamed("_1", "language")
            .withColumnRenamed("_2", "percent")

    renamedResult.orderBy("percent").show(false)
  }

  def jsonDemo(spark: SparkSession) = {

    import spark.implicits._
    val dataFrame = spark.read.json(path)
    println(dataFrame.describe())
    println(dataFrame.take(5).mkString(","))
    dataFrame.filter(dataFrame.col("pop") > 40000).show(10)
    dataFrame.createOrReplaceTempView("zips_table")
    dataFrame.cache()
    spark.sql("select city, pop, state, _id from zips_table").show(10)
    println()

    spark.sql("use mydb")
    spark.sql("DROP TABLE IF EXISTS iteblog_hive")
    spark.table("zips_table").write.saveAsTable("iteblog_hive")

    val dataRdd = spark.sql("select * from iteblog_hive limit 20").rdd
    dataRdd.foreach(println)
    println(s"dataRdd:${dataRdd.collect().mkString("$$")}")

    Thread.sleep(60000)

  }
}
