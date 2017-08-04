package edu.illinois.harrisonkiang.topicmodel

import java.util.UUID

import edu.illinois.harrisonkiang.feeds.rss.GoogleNewsObj
import edu.illinois.harrisonkiang.sentiment.{GoogleNewsSentencesAndSentiments, GoogleNewsSentencesAndSentimentsObj}
import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicModel, TopicTickerTable}
import org.postgresql.util.PGobject

import scala.collection.mutable.ArrayBuffer

class GoogleNewsTopicModel extends TopicTickerTable with TopicModel {

  // enables uuid_generate_v4
  connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")

  override val tableName: String = "googlenews_topicmodel"

  val DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
  override val schema: Schema = Schema(Array(
    SchemaCol("id", "uuid DEFAULT uuid_generate_v4 ()"),
    SchemaCol("googlenews_id", "uuid"),
    SchemaCol("words", "text[]"),
    SchemaCol("probability", "double precision[]")
  ))
  override val uniqueConstraint: String = "link"

  var data: ArrayBuffer[GoogleNewsTopicModelObj] = ArrayBuffer()

  override def createTableStatement: String = super.createTableStatement
  override def queryHeaderForInsertRecords: String = super.queryHeaderForInsertRecords

  override def insertRecords(forceOpenConenction: Boolean = false): Unit = {
    ensureTableExists()

    val nonConflictingInsertQuery = queryHeaderForInsertRecords.concat(
      " ON CONFLICT DO NOTHING"
    )

    val stmt = connection.prepareStatement(nonConflictingInsertQuery)

    if(data != null) {
      data.foreach(println)
      data.foreach(datum => {
        val googlenews_idPgObject: PGobject = new PGobject()
        googlenews_idPgObject.setType("uuid")
        googlenews_idPgObject.setValue(datum.googlenews_id.toString)
        stmt.setObject(1, googlenews_idPgObject)

        stmt.setArray(2, datum.words)
        stmt.setArray(3, datum.probabilities)
        stmt.addBatch()
      })
    }

    logger.info(stmt.toString)

    stmt.executeBatch()
    if(!forceOpenConenction) {
      stmt.close()
    }
    data = null
  }

  override def dropTableStatement: String = super.dropTableStatement

  def updateData(forceOpenConnection: Boolean = false): Unit = {
    ensureConnectionIsOpen()
    val googleNewsSentencesAndSentiments = new GoogleNewsSentencesAndSentiments()
    val rs = googleNewsSentencesAndSentiments.getRecords(forceOpenConnection = true)
    while(rs.next()){
      val googlenews_id = rs.getObject("googlenews_id").asInstanceOf[UUID]
      val sentences = rs.getArray("sentences").asInstanceOf[Array[String]]

      val topicModel = getTopicModelForText(sentences)
      logger.info(topicModel.mkString("\t"))

      val words = connection.createArrayOf("text", topicModel.map(_._1))
      val probabilities = connection.createArrayOf("double precision", topicModel.map(_._2).asInstanceOf[Array[AnyRef]])
      logger.info(words)
      logger.info(probabilities)

      this.data += GoogleNewsTopicModelObj(googlenews_id, words, probabilities)
  }
    insertRecords(forceOpenConnection)
  }

  override def updateTableWithFreshData(): Unit = {
    updateData()
  }
}
