package edu.illinois.harrisonkiang.topicmodel

import java.lang
import java.util.UUID

import edu.illinois.harrisonkiang.sentiment.GoogleNewsSentencesAndSentiments
import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicModel, TopicTickerTable}
import org.postgresql.util.PGobject

import scala.collection.mutable.ArrayBuffer

class GoogleNewsTopicModel extends TopicTickerTable with TopicModel {

  // enables uuid_generate_v4
  connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")
  val googleNewsSentencesAndSentiments = new GoogleNewsSentencesAndSentiments()

  override val tableName: String = "googlenews_topicmodel"

  val DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
  override val schema: Schema = Schema(Array(
    SchemaCol("id", "uuid DEFAULT uuid_generate_v4 ()"),
    SchemaCol("googlenews_id", "uuid"),
    SchemaCol("word", "text"),
    SchemaCol("probability", "double precision")
  ))
  override val uniqueConstraint: String = "googlenews_id, word"

  var data: ArrayBuffer[GoogleNewsTopicModelObj] = ArrayBuffer()

  override def createTableStatement: String = super.createTableStatement
  override def queryHeaderForInsertRecords: String = super.queryHeaderForInsertRecords

  override def insertRecords(forceOpenConnection: Boolean = false): Unit = {
    ensureTableExists()

    val nonConflictingInsertQuery = queryHeaderForInsertRecords.concat(
      " ON CONFLICT DO NOTHING"
    )

    val stmt = connection.prepareStatement(nonConflictingInsertQuery)

    if(data != null) {
//      data.foreach(println)
      data.foreach(datum => {
        val googlenews_idPgObject: PGobject = new PGobject()
        googlenews_idPgObject.setType("uuid")
        googlenews_idPgObject.setValue(datum.googlenews_id.toString)
        stmt.setObject(1, googlenews_idPgObject)

        stmt.setString(2, datum.word)
        stmt.setObject(3, datum.probability)
        stmt.addBatch()
      })
    }

    logger.info(s"stmt: ${stmt.toString}")

    stmt.executeBatch()
    if(!forceOpenConnection) {
      stmt.close()
    }
    data = ArrayBuffer()
  }

  override def dropTableStatement: String = super.dropTableStatement

  def updateBatch(batchSize: Int = 1): Boolean = {
    val rs = googleNewsSentencesAndSentiments.getRecords(forceOpenConnection = true, limit = batchSize)

    var batchesRemaining = false

    while(rs.next()) {
      val googlenews_id = rs.getObject("googlenews_id").asInstanceOf[UUID]
      val sentences = rs.getArray("sentences").toString.drop(1).dropRight(1).split("\",\"")
      val topicModel = getTopicModelForText(sentences)
//      logger.info(topicModel.mkString("\n"))

      topicModel.foreach(e => {
        val newElem = GoogleNewsTopicModelObj(googlenews_id, e._1, e._2)
        data += newElem
      })
//      logger.info(this.data.mkString("\n"))
      batchesRemaining = true
    }
    insertRecords()
    batchesRemaining
  }

  def updateData(forceOpenConnection: Boolean = false): Unit = {
    var batchesRemaining = true
    while(batchesRemaining) {
      batchesRemaining = updateBatch()
    }
  }

  override def updateTableWithFreshData(forceOpenConnection: Boolean = false): Unit = {
    updateData(forceOpenConnection)
    insertRecords()
    if(!forceOpenConnection) {
      connection.close()
    }
  }
}
