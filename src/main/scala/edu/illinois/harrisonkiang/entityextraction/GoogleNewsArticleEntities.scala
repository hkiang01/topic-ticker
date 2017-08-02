package edu.illinois.harrisonkiang.entityextraction

import java.sql.ResultSet
import java.util.UUID

import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicTickerLogger, TopicTickerTable}

import scala.collection.mutable.ArrayBuffer

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

  override def insertRecords(forceOpenConnection: Boolean = false): Unit = {
    ensureTableExists()

    val nonConflictingInsertQuery = queryHeaderForInsertRecords.concat(
      " ON CONFLICT DO NOTHING"
    )

    connection.setAutoCommit(false)

    data.foreach(datum => {
      val googleNewsId = datum.googlenews_id

      datum.sentences.foreach(sentence => {
        val entitiesAndTypes = extractEntities(sentence)
        ensureConnectionIsOpen()
        val stmt = connection.prepareStatement(nonConflictingInsertQuery)

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
        if(!forceOpenConnection) {
          stmt.close()
        }
      })
    })
    data = ArrayBuffer()
  }

  def googleNewsIdsAndSentencesWithoutEntities(numRecords: Int = Integer.MAX_VALUE, forceOpenConnection: Boolean = false): ResultSet = {
    ensureConnectionIsOpen()
    ensureTableExists()
    val stmt = connection.createStatement()
    val sql = s"select googlenews_id, sentences from googlenews_sentenceandsentiments " +
      s"where googlenews_id not in (select googlenews_id from $tableName) LIMIT $numRecords"
    val results =stmt.executeQuery(sql)
    if(!forceOpenConnection) {
      stmt.close()
    }
    results
  }

  def updateBatch(batchSize: Int = 1): Boolean = {
    ensureConnectionIsOpen()
    ensureTableExists()
    val resultSet = googleNewsIdsAndSentencesWithoutEntities(batchSize, forceOpenConnection = true)
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
