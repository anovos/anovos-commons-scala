package anovos.data.transformer

import anovos.util.SparkSessionUtil
import org.apache.spark.sql.SaveMode
import org.scalatest._
import scala.collection.JavaConverters._
import org.apache.spark.sql.functions.col

class CatToNumUnsupervisedSpec extends FunSuite with BeforeAndAfter {

  test("Test CatToNumUnsupervised.apply label_encoding") {
    implicit val spark = SparkSessionUtil.getLocalSparkSession("CatToNumUnsupervised.apply")

    val inputPath = "./data/test_dataset/part-00001-3eb0f7bb-05c2-46ec-8913-23ba231d2734-c000.snappy.parquet"
    val outputPath = "./data/output/"
    val modelPath = "./data/model/"
    val indexOrder = "frequencyDesc"

    var methodType = "label_encoding"

    val listOfCols = List("workclass","education","marital-status","occupation", "relationship", "sex")
    val listOfColumns = new java.util.ArrayList[String](listOfCols.asJava)

    val df = spark.read.parquet(inputPath)//.select(listOfCols.map(m=>col(m)):_*)

    val catToNumUnsupervisedObj = new CatToNumUnsupervised(spark.sqlContext, df)

    val odf = catToNumUnsupervisedObj.apply(methodType = methodType, indexOrder = indexOrder,
      listOfColumns = listOfColumns, preExistingModel = false, modelPath = modelPath, outputMode = "append", printImpact = true)

    odf.show()
    odf.write.option("header",true).mode(SaveMode.Overwrite).csv(outputPath+"label_encoding")

  }

  test("Test CatToNumUnsupervised.apply onehot_encoding") {
    implicit val spark = SparkSessionUtil.getLocalSparkSession("CatToNumUnsupervised.apply")

    val inputPath = "./data/test_dataset/part-00001-3eb0f7bb-05c2-46ec-8913-23ba231d2734-c000.snappy.parquet"
    val outputPath = "./data/output/"
    val modelPath = "./data/model/"
    val indexOrder = "frequencyDesc"

    var methodType = "onehot_encoding"

    val listOfCols = List("workclass","education","marital-status","occupation", "relationship", "sex")
    val listOfColumns = new java.util.ArrayList[String](listOfCols.asJava)

    val df = spark.read.parquet(inputPath)//.select(listOfCols.map(m=>col(m)):_*)

    val catToNumUnsupervisedObj = new CatToNumUnsupervised(spark.sqlContext, df)

    val odf = catToNumUnsupervisedObj.apply(methodType = methodType, indexOrder = indexOrder,
      listOfColumns = listOfColumns, preExistingModel = false, modelPath = modelPath , outputMode = "append", printImpact = true)

    odf.show()
    odf.write.option("header",true).mode(SaveMode.Overwrite).csv(outputPath+"onehot_encoding")

  }

}
