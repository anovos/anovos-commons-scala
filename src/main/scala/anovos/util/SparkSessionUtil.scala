package anovos.util

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkSessionUtil {

  def getLocalSparkSession(name: String) = {
    val conf = new SparkConf()
    conf.set("spark.master", "local")
    implicit val spark = SparkSession.builder.appName(name).config(conf).getOrCreate()
    val sc = spark.sparkContext

    spark
  }

}
