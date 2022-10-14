package anovos.data.transformer

import org.apache.spark.ml.feature.{StringIndexer, StringIndexerModel}
import org.apache.spark.ml.feature.{OneHotEncoderEstimator => OneHotEncoder}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.types.IntegerType

import java.util
import scala.collection.mutable.ListBuffer

class CatToNumUnsupervised(sqlContext: SQLContext, df: DataFrame) {
  import sqlContext.implicits._
  import org.apache.spark.sql.functions._
  import scala.collection.JavaConverters._

  def apply(methodType : String = "label_encoding", indexOrder: String = "frequencyDesc",
            listOfColumns : util.ArrayList[String], skipColumns : util.ArrayList[String] = null,
            preExistingModel : Boolean = false , modelPath : String = "NA",
            outputMode : String = "replace", printImpact : Boolean = false): DataFrame = {

    val listOfCols = listOfColumns.asScala.toArray
    val listOfColsIndex = listOfCols.map(m => m+"_index")
    val listOfColsVec = listOfCols.map(m => m+"_vec")

    var odfIndexed = df
    var indexerModel:StringIndexerModel = null
    var stringIndexer = new StringIndexer()
    for (i <- listOfCols) {
      if (preExistingModel) {
        indexerModel = StringIndexerModel.load(modelPath + "/cat_to_num_unsupervised/indexer-model-" + i)
      } else {
        stringIndexer = new StringIndexer().setStringOrderType(indexOrder).setHandleInvalid("keep")
          .setInputCol(i).setOutputCol(i + "_index")

        indexerModel = stringIndexer.fit(df.select(i))

        if (!"NA".equalsIgnoreCase(modelPath)) {
          indexerModel.write.overwrite.save(modelPath + "/cat_to_num_unsupervised/indexer-model-" + i)
        }
      }
      odfIndexed = indexerModel.transform(odfIndexed)
    }

    val newColumnsList = ListBuffer[String]()
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

      val sample_row = odf.take(1)
      listOfCols.foreach(colName => {
        val uniqCats = sample_row(0).getAs[SparseVector](colName + "_vec").size
        for( i <- 0 to uniqCats-1){
          newColumnsList.append(colName + "-" + i)
        }
        odf = odf.select(
          odf.col("*") +: (0 until uniqCats).map(i => vectorToArrUdf(col(colName + "_vec"))(i).alias(s"$colName-$i")): _*
        )
        if("replace".equalsIgnoreCase(outputMode)){
          odf = odf.drop(colName, colName + "_vec", colName + "_index")
        }else{
          odf = odf.drop(colName + "_vec", colName + "_index")
        }
      })
    }else{
      for (i <- listOfCols){
        odf = odf.withColumn(
          i + "_index",
          when(col(i).isNull, -1).otherwise(
            col(i + "_index").cast(IntegerType)
          ))
      }
      if("replace".equalsIgnoreCase(outputMode)){
        for (i <- listOfCols){
          odf = odf.drop(i)
            .withColumnRenamed(i+"_index", i)
        }
        odf = odf.select(df.columns.map(m=>col(m)):_*)
      }
    }
    var newColumns = newColumnsList.toList.toArray
    if(printImpact){
      if("onehot_encoding".equalsIgnoreCase(methodType)){
        println("Before")
        df.select(listOfCols.map(m=>col(m)):_*).printSchema()
        println("After")
        if("append".equalsIgnoreCase(outputMode)){
          val allCols = Array.concat(listOfCols, newColumns)
          odf.select(allCols.map(m=>col(m)):_*).printSchema()
        }else{
          odf.select(newColumns.map(m=>col(m)):_*).printSchema()
        }
      }else{
        var newCols = Array[String]()
        if("append".equalsIgnoreCase(outputMode)){
          for(column <- listOfCols){newColumnsList.append(column + "_index")}
          newCols = newColumnsList.toList.toArray
        }else{
          newCols = listOfCols
        }
        println("Before")
        df.select(listOfCols.map(m=>col(m)):_*).summary("count", "min", "max").show(3, false)
        println("After")
        odf.select(newCols.map(m=>col(m)):_*).summary("count", "min", "max").show(3, false)
      }
      if(skipColumns != null){
        println("Columns dropped from encoding due to high cardinality: "+ skipColumns.asScala.toArray.mkString(","))
      }
    }
    //odf.show()
    odf
  }
}

