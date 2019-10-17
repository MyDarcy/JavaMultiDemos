package com.darcy.spark

import java.io.{FileOutputStream, InputStream, PrintWriter}

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.IOUtils
import org.apache.spark.SparkContext
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext

import scala.io.Source


object HdfsUtils extends Serializable with Logging {

  /**
    * 将数据存入HDFS目录中
    *
    * @param sc
    * @param searchRdd
    */
  def saveDataToHDFS(sc: SparkContext, location: String, searchRdd: RDD[String]): Unit = {
    // 重跑时数据已存在, 先删除
    if (HdfsUtils.existsDir(sc, location)) {
      HdfsUtils.deleteDir(sc, location)
    }
    searchRdd.saveAsTextFile(location)
  }

  /**
    * 监测hdfs文件是否存在
    *
    * @paramsc
    * @paramfilePath
    * @return
    */
  def hdfsFileExist(sc: SparkContext, filePath: String): Boolean = {
    val conf = sc.hadoopConfiguration
    val fs = org.apache.hadoop.fs.FileSystem.get(conf)
    val exists = fs.exists(new org.apache.hadoop.fs.Path(filePath))
    exists
  }

  /**
    * 将数据保存到hdfs文件
    *
    * @parampath
    */
  def writeDataToHdfs(sc: SparkContext, path: String, data: String) {
    println("Trying to write to HDFS...")
    val conf = sc.hadoopConfiguration
    val fs = org.apache.hadoop.fs.FileSystem.get(conf)
    val output = fs.create(new org.apache.hadoop.fs.Path(path))
    val writer = new PrintWriter(output)
    print("write data to hdfs")
    try {
      writer.write(data)
    }
    finally {
      writer.close()
    }
    print("Done!")
  }

  def hdfsFileCopytoLocal(hadoopFile: String, localFile: String, sc: SparkContext) = {
    val conf = sc.hadoopConfiguration
    val fs = org.apache.hadoop.fs.FileSystem.get(conf)
    val inputStream = fs.open(new Path(hadoopFile))
    val output = new FileOutputStream(localFile)
    IOUtils.copyBytes(inputStream, output, 4096, true)
  }


  def getHiveTablePath(tableName: String, sqlContext: HiveContext):String =
  {
    val sql = String.format("desc formatted %s", tableName)
    val sql_ret = sqlContext.sql(sql).filter("result like 'Location:%'")
    val info = sql_ret.collect().mkString(" ")
    val path = info.split('\t')(1)
    path
  }

  def deleteDir(sc: SparkContext, path: String): Unit = {
    log.info("to delete data:" + path)
    val fs = FileSystem.get(sc.hadoopConfiguration)
    if(hdfsFileExist(sc, path)){
      fs.delete(new Path(path), true)
      fs.close()
    }else{
      println("deleteDir failed path " + path + "not exit")
    }

  }

  def renameDir(sc: SparkContext, pathOld: String, pathNew: String): Unit = {
    log.info("to rename " + pathOld + " -> " + pathNew)
    val fs = FileSystem.get(sc.hadoopConfiguration)
    fs.rename(new Path(pathOld), new Path(pathNew))
    fs.close()
  }

  def generateTmpDir(path: String): String = {
    val timestamp = System.currentTimeMillis() / 1000
    var tmpPath = path.stripSuffix("/")
    tmpPath += "_" + timestamp
    tmpPath
  }


  def makeDir(sc: SparkContext, path: String): Unit = {
    log.info("to make dir:" + path)
    val fs = FileSystem.get(sc.hadoopConfiguration)
    fs.mkdirs(new Path(path))
    fs.close()
  }

  def existsDir(sc: SparkContext, path: String): Boolean = {
    log.info("to test dir exists:" + path)
    val fs = FileSystem.get(sc.hadoopConfiguration)
    var isExists = false
    if (fs.exists(new Path(path))) {
      isExists = true
    }
    fs.close()
    isExists
  }


  def deleteThenMakeDir(sc: SparkContext, path: String): Unit = {
    val fs = FileSystem.get(sc.hadoopConfiguration)
    log.info("to delete data:" + path)
    fs.delete(new Path(path), true)
    log.info("to make dir:" + path)
    fs.mkdirs(new Path(path))
    fs.close()
  }

  def parseTaskConf(confType: String, confContent: String): Map[String, String] = {
    log.info("Loading from conf:" + confType)
    var confDict = Map[String, String]()
    val confRecords = if (confType == "-s") {
      confContent.split(";").toList
    }
    else {
      Source.fromFile(confContent, "utf8").getLines()
    }

    for (line <- confRecords) {
      if (line.length > 0 && line(0) != '#') {
        // 注释过滤
        val kvs = line.trim()
        if (kvs.length >= 2) {
          // 空行过滤
          if (kvs.contains("=") && !kvs.startsWith("=")) {
            val fields = kvs.split("=")
            val k = fields(0).trim().replace("\"", "")
            var v = "" //允许value为空
            if (fields.length == 2) {
              v = fields(1).trim().replace("\"", "")
            }
            if (k.length >= 1 && v.length >= 0) {
              log.info("NPITask: LoadConf:[" + k + "=" + v + "]")
              confDict += (k -> v)
            }
            else {
              log.warn("invalid conf:" + kvs)
            }
          }
          else {
            log.warn("invalid conf:" + kvs)
          }
        }
      }
    }
    confDict
  }
}
