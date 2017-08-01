package edu.illinois.harrisonkiang.entityextraction

import java.sql.ResultSet
import java.util.UUID

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicTickerLogger, TopicTickerTable}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, TimeoutException, future}

case class GoogleNewsArticleIdsAndSentences(googlenews_id: UUID, sentences: Array[String])
case class GoogleNewsArticleEntitiesObj(googlenews_id: UUID, entity: String, entity_type: String)

class GoogleNewsArticleEntities extends TopicTickerTable with EntityExtractor with TopicTickerLogger {

  // enables uuid_generate_v4
  connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")

  override val tableName: String = "googlenews_article_entities"

  override val schema: Schema = Schema(Array(
    SchemaCol("id", "uuid DEFAULT uuid_generate_v4 ()"),
    SchemaCol("googlenews_id", "uuid"),
    SchemaCol("entity", s"text"),
    SchemaCol("entity_type", s"varchar(255)")
  ))
  override val uniqueConstraint: String = "googlenews_id, entity"

  var data: ArrayBuffer[GoogleNewsArticleIdsAndSentences] = ArrayBuffer()

  override def createTableStatement: String = super.createTableStatement
  override def queryHeaderForInsertRecords: String = super.queryHeaderForInsertRecords
  override def dropTableStatement: String = super.dropTableStatement

  override def insertRecords(): Unit = {
    ensureTableExists()

    val nonConflictingInsertQuery = queryHeaderForInsertRecords.concat(
      " ON CONFLICT DO NOTHING"
    )

    connection.setAutoCommit(false)
    val stmt = connection.prepareStatement(nonConflictingInsertQuery)

    data.foreach(datum => {
      val googleNewsId = datum.googlenews_id

      datum.sentences.foreach(sentence => {

        var entitiesAndTypes: Array[(String, String)] = Array.empty

        lazy val f = Future{
          entitiesAndTypes = extractEntities(sentence)
          if(entitiesAndTypes.nonEmpty) {
            entitiesAndTypes.foreach(entityAndType => {
              val entity = entityAndType._1
              val entityType = entityAndType._2

              stmt.setObject(1, googleNewsId)
              stmt.setString(2, entity)
              stmt.setString(3, entityType)
              stmt.addBatch()
            })
            logger.info(stmt.toString)
            stmt.executeBatch()
          }
        }

        try {
          Await.result(f, 1 minute)
        } catch {
          case te: TimeoutException => {
            logger.warn("timed out")
          }
          case e: Exception => {
            logger.error("exception!")
          }
        } finally {

        }
      })
    })
    data = ArrayBuffer()
  }

  def googleNewsIdsAndSentencesWithoutEntities(numRecords: Int = Integer.MAX_VALUE): ResultSet = {
    ensureTableExists()
    val stmt = connection.createStatement()
    val sql = s"select googlenews_id, sentences from googlenews_sentenceandsentiments " +
      s"where googlenews_id not in (select googlenews_id from $tableName) LIMIT $numRecords"
    stmt.executeQuery(sql)
  }

  def updateBatch(batchSize: Int = 1): Boolean = {
    ensureTableExists()
    val resultSet = googleNewsIdsAndSentencesWithoutEntities(batchSize)
    var result = false
    while(resultSet.next()) {
      result = true
      val googleNewsId = resultSet.getObject("googlenews_id").asInstanceOf[UUID]
      val sentencesRaw = resultSet.getArray("sentences")
      if(sentencesRaw != null) {
        val sentences = sentencesRaw.getArray.asInstanceOf[Array[String]]
        data += GoogleNewsArticleIdsAndSentences(googleNewsId, sentences)
      }
      insertRecords()
    }
    result
  }

  def updateData(): Unit = {
    var batchesRemaining = true
    while(batchesRemaining) {
      batchesRemaining = updateBatch()
    }
  }

  override def updateTableWithFreshData(): Unit = {
    updateData()
    insertRecords()
  }

}
