package anovos.data.transformer

import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, StringIndexerModel}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.{DataFrame, SQLContext}

import java.util

class CatToNumUnsupervised(sqlContext: SQLContext, df: DataFrame) {
  import sqlContext.implicits._
  import org.apache.spark.sql.functions._
  import scala.collection.JavaConverters._

  def apply(methodType : String = "label_encoding", indexOrder: String = "frequencyDesc",
            listOfColumns : util.ArrayList[String], preExistingModel : Boolean = false ,
            modelPath : String = "NA", outputMode : String = "replace", printImpact : Boolean = false): DataFrame = {

    val listOfCols = listOfColumns.asScala.toArray
    val listOfColsIndex = listOfCols.map(m => m+"_index")
    val listOfColsVec = listOfCols.map(m => m+"_vec")

    var indexerModel:StringIndexerModel = null
    if(preExistingModel){
      indexerModel = StringIndexerModel.load(modelPath + "/cat_to_num_unsupervised/indexer")
    }else{
      indexerModel = new StringIndexer().setStringOrderType(indexOrder).setHandleInvalid("keep")
        .setInputCols(listOfCols).setOutputCols(listOfColsIndex).fit(df)

      if(!"NA".equalsIgnoreCase(modelPath)) {
        indexerModel.write.overwrite.save(modelPath + "/cat_to_num_unsupervised/indexer")
      }
    }

    val odfIndexed = indexerModel.transform(df)

    var odf = odfIndexed
    if("onehot_encoding".equalsIgnoreCase(methodType)) {
      var oneHotEncoder:OneHotEncoder = null
      if(preExistingModel){
        oneHotEncoder = OneHotEncoder.load(modelPath + "/cat_to_num_unsupervised/encoder")
      }else{
        oneHotEncoder = new OneHotEncoder().setHandleInvalid("keep").setInputCols(listOfColsIndex).setOutputCols(listOfColsVec)
        if(!"NA".equalsIgnoreCase(modelPath)) {
          oneHotEncoder.write.overwrite.save(modelPath + "/cat_to_num_unsupervised/encoder")
        }
      }

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
    //odf.show()
    odf
  }
}

