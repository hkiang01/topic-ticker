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

    data.foreach(datum => {
      val googleNewsId = datum.googlenews_id
      logger.info(s"exceuting insertRecords into $tableName for googleNewsId: $googleNewsId...")

      if (datum != null && datum.sentences != null && datum.sentences.length > 0) {
        datum.sentences.foreach(sentence => {
          logger.info(s"sentence: $sentence")
          val entitiesAndTypes = extractEntities(sentence)
          ensureConnectionIsOpen()
          val stmt = connection.prepareStatement(nonConflictingInsertQuery)

          entitiesAndTypes.foreach(entityAndType => {
            val entity = entityAndType._1
            val entityType = entityAndType._2
            logger.info(s"entity: '$entity\tentityType: $entityType")

            stmt.setObject(1, googleNewsId)
            stmt.setString(2, entity)
            stmt.setString(3, entityType)
            stmt.addBatch()
          })
          logger.info(stmt.toString)
          stmt.executeBatch()
        })
      } else {
        logger.warn(s"datum or datum.sentences null or empty, queuing empty sentences for $googleNewsId")
        val stmt = connection.prepareStatement(nonConflictingInsertQuery)
        stmt.setObject(1, googleNewsId)
        stmt.setString(2, null)
        stmt.setString(3, null)
        stmt.addBatch()
        logger.info(stmt.toString)
        stmt.executeBatch()
      }
      //      if(!forceOpenConnection) {
      //        connection.close()
      //      }
      logger.info(s"executed insertRecords into $tableName for googleNewsId: $googleNewsId")
    })
    data = ArrayBuffer()
  }

  def googleNewsIdsAndSentencesWithoutEntities(numRecords: Int = Integer.MAX_VALUE, forceOpenConnection: Boolean = false): ResultSet = {
    ensureConnectionIsOpen()
    ensureTableExists()
    val stmt = connection.createStatement()
    val sql = s"select googlenews_id, sentences from googlenews_sentenceandsentiments " +
      s"where googlenews_id not in (select googlenews_id from $tableName) LIMIT $numRecords"
    val results = stmt.executeQuery(sql)
    if (!forceOpenConnection) {
      stmt.close()
    }
    results
  }

  def updateBatch(batchSize: Int = 1): Boolean = {
    ensureConnectionIsOpen()
    ensureTableExists()
    val resultSet = googleNewsIdsAndSentencesWithoutEntities(batchSize, forceOpenConnection = true)
    var result = false
    while (resultSet.next()) {
      result = true
      val googleNewsId = resultSet.getObject("googlenews_id").asInstanceOf[UUID]
      logger.info(s"batch googleNewsId: $googleNewsId")
      val sentencesRaw = resultSet.getArray("sentences")
      if (sentencesRaw != null) {
        val sentences = sentencesRaw.getArray.asInstanceOf[Array[String]]
        data += GoogleNewsArticleIdsAndSentences(googleNewsId, sentences)
      } else {
        logger.warn(s"sentences null, queuing empty sentences for $googleNewsId")
        data += GoogleNewsArticleIdsAndSentences(googleNewsId, null)
      }
      insertRecords()
    }
    result
  }

  def updateData(): Unit = {
    var batchesRemaining = true
    while (batchesRemaining) {
      batchesRemaining = updateBatch()
      insertRecords()
    }
  }

  override def updateTableWithFreshData(): Unit = {
    updateData()
    insertRecords()
  }

}
