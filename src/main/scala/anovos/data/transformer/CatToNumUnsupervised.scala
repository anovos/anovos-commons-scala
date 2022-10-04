package anovos.data.transformer

import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.{DataFrame, SQLContext}

import java.util

class CatToNumUnsupervised(sqlContext: SQLContext, df: DataFrame) {
  import sqlContext.implicits._
  import org.apache.spark.sql.functions._
  import scala.collection.JavaConverters._

  def apply(methodType : String, listOfColumns : util.ArrayList[String]): DataFrame = {
    val listOfCols = listOfColumns.asScala.toArray
    val listOfColsIndex = listOfCols.map(m => m+"_index")
    val listOfColsVec = listOfCols.map(m => m+"_vec")

    val odfIndexed = new StringIndexer().setHandleInvalid("keep").setInputCols(listOfCols).setOutputCols(listOfColsIndex).fit(df).transform(df)
    var odf = odfIndexed
    if("onehot_encoding".equalsIgnoreCase(methodType)) {
      val oneHotEncoder = new OneHotEncoder().setHandleInvalid("keep").setInputCols(listOfColsIndex).setOutputCols(listOfColsVec)
      odf = oneHotEncoder.fit(odfIndexed).transform(odfIndexed)

      val convertVectorToArr: Any => Array[Int] = _.asInstanceOf[SparseVector].toArray.map(_.toInt)
      val vectorToArrUdf = udf(convertVectorToArr)

      odf = odf.drop(listOfColsIndex.toSeq: _*)
      val sample_row = odf.take(1)
      listOfCols.foreach(colName => {
        val uniqCats = sample_row(0).getAs[SparseVector](colName + "_vec").size
        odf = odf.select(
          odf.col("*") +: (0 until uniqCats).map(i => vectorToArrUdf(col(colName + "_vec"))(i).alias(s"$colName-$i")): _*
        )
      })
      odf = odf.drop(listOfColsVec.toSeq: _*)
    }else{
      for (i <- listOfCols){
        odf = odf.withColumn(
          i + "_index",
          when(col(i).isNull, col(i)).otherwise(
            col(i + "_index").cast("Integer")
          ))
      }
    }
    odf
  }
}

