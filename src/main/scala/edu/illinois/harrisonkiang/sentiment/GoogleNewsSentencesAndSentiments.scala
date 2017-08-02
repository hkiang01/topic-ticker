package edu.illinois.harrisonkiang.sentiment

import java.sql.ResultSet
import java.util.UUID

import edu.illinois.harrisonkiang.textextraction.TextExtraction
import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicTickerLogger, TopicTickerTable}

import scala.collection.mutable.ArrayBuffer

class GoogleNewsSentencesAndSentiments extends TopicTickerTable with TextExtraction with SentimentAnalyzer with TopicTickerLogger {

  // enables uuid_generate_v4
  connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")

  override val tableName: String = "googlenews_sentenceandsentiments"

  Sentiment.createSentimentEmum()

  override val schema: Schema = Schema(Array(
    SchemaCol("id", "uuid DEFAULT uuid_generate_v4 ()"),
    SchemaCol("googlenews_id", "uuid"),
    SchemaCol("sentences", s"text[]"),
    SchemaCol("sentiments", s"sentiment[]")
  ))
  override val uniqueConstraint: String = "googlenews_id"

  var data: ArrayBuffer[GoogleNewsSentencesAndSentimentsObj] = ArrayBuffer()

  override def createTableStatement: String = super.createTableStatement
  override def queryHeaderForInsertRecords: String = super.queryHeaderForInsertRecords
  override def dropTableStatement: String = super.dropTableStatement

  override def insertRecords(forceOpenConnection: Boolean = false): Unit = {
    ensureTableExists()

    val nonConflictingInsertQuery = queryHeaderForInsertRecords.concat(
      " ON CONFLICT DO NOTHING"
    )

    connection.setAutoCommit(false)
    val stmt = connection.prepareStatement(nonConflictingInsertQuery)

    data.foreach(datum => {
      stmt.setObject(1, datum.googlenews_id)
      stmt.setArray(2, datum.sentences)
      stmt.setArray(3, datum.sentiments)
      stmt.addBatch()
    })

    logger.info(stmt.toString)

    stmt.executeBatch()
    if(!forceOpenConnection) {
      stmt.close()
    }
    data = ArrayBuffer()
  }

  def googleNewsIdsAndLinksWithoutTextAndSentiment(numRecords: Int = Integer.MAX_VALUE): ResultSet = {
    ensureTableExists()
    val stmt = connection.createStatement()
    val sql = s"select id, link from googlenews where id not in (select googlenews_id from $tableName) LIMIT $numRecords"
    stmt.executeQuery(sql)
  }

  def updateBatch(batchSize: Int = 5): Boolean = {
    ensureTableExists()
    val resultSet = googleNewsIdsAndLinksWithoutTextAndSentiment(5)
    var result = false
    while(resultSet.next()) {
        result = true
        val googleNewsId = resultSet.getObject[java.util.UUID]("id", classOf[UUID])
        val link = resultSet.getString("link")
        logger.info(s"processing $link")

        val articleText = getArticleText(link)
        logger.info(s"articleText: ${articleText.take(200)}...")

        try {
          val sentencesAndSentiments = extractSentiments(articleText)
          val sentences = sentencesAndSentiments.map(_._1)
          val sentencesSqlArr = connection.createArrayOf("text", sentences.toArray)

          val sentiments = sentencesAndSentiments.map(_._2)
          val sentimentsSqlArr = connection.createArrayOf("sentiment", sentiments.toArray)

          val newElem = GoogleNewsSentencesAndSentimentsObj(googleNewsId, sentencesSqlArr, sentimentsSqlArr)
          data += newElem
          insertRecords()
        } catch {
          case (oom: OutOfMemoryError) => {
            logger.warn(s"out of memory, queuing empty sentences and sentiments for $link")
            data += GoogleNewsSentencesAndSentimentsObj(googleNewsId, null, null)
          } case (e: Exception) => {
            e.printStackTrace()
            logger.warn(s"exception, queuing empty sentences and sentiments for $link")
            data += GoogleNewsSentencesAndSentimentsObj(googleNewsId, null, null)
          }
        }
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
