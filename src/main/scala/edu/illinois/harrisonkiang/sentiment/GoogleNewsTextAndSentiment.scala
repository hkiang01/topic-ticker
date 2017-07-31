package edu.illinois.harrisonkiang.sentiment

import java.sql.ResultSet
import java.util.UUID

import edu.illinois.harrisonkiang.textextraction.TextExtraction
import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicTickerLogger, TopicTickerTable}

case class GoogleNewsUUIDLink(googlenews_id: UUID, link: String)
case class GoogleNewsSentencesAndSentimentsObj(googlenews_id: UUID, sentences: java.sql.Array, sentiments: java.sql.Array)

class GoogleNewsSentencesAndSentiment extends TopicTickerTable with TextExtraction with SentimentAnalyzer with TopicTickerLogger {

  // enables uuid_generate_v4
  connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")

  override val tableName: String = "googlenews_sentenceandsentiment"

  override val schema: Schema = Schema(Array(
    SchemaCol("id", "uuid DEFAULT uuid_generate_v4 ()"),
    SchemaCol("googlenews_id", "uuid"),
    SchemaCol("sentences", s"text[]"),
    SchemaCol("sentiments", s"sentiment[]")
  ))
  override val uniqueCol: String = "id"

  var data: Array[GoogleNewsSentencesAndSentimentsObj] = Array()

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
      stmt.setObject(1, datum.googlenews_id)
      stmt.setArray(2, datum.sentences)
      stmt.setArray(3, datum.sentiments)
      stmt.addBatch()
    })

    logger.info(stmt.toString)

    stmt.executeBatch()
    data = null
  }

  def googleNewsIdsAndLinksWithoutTextAndSentiment(numRecords: Int): ResultSet = {
    ensureTableExists()
    val stmt = connection.createStatement()
    val sql = s"select id, link from googlenews where id not in (select googlenews_id from $tableName) LIMIT $numRecords"
    stmt.executeQuery(sql)
  }

  def updateSingleData(): Unit = {
    ensureTableExists()
    val resultSet = googleNewsIdsAndLinksWithoutTextAndSentiment(1)
    while(resultSet.next()) {
      val googlenews_id = resultSet.getObject[java.util.UUID]("id", classOf[UUID])
      val link = resultSet.getString("link")
      val articleText = getArticleText(link)

      val sentencesAndSentiments = extractSentiments(articleText)

      val sentences = sentencesAndSentiments.map(_._1)
      val sentencesSqlArr = connection.createArrayOf("text", sentences.toArray)

      val sentiments = sentencesAndSentiments.map(_._2)
      val sentimentsSqlArr = connection.createArrayOf("sentiment", sentiments.toArray)

      logger.info(sentences.toString())
      logger.info(sentiments.toString())
      logger.info(sentencesSqlArr.toString)
      logger.info(sentimentsSqlArr.toString)

      val newElem = GoogleNewsSentencesAndSentimentsObj(googlenews_id, sentencesSqlArr, sentimentsSqlArr)
      data.patch(data.length, Array(newElem), 0)
    }
  }

  def updateData(): Unit = {
    ensureTableExists()
    val resultSet = googleNewsIdsAndLinksWithoutTextAndSentiment(20)
    while(resultSet.next()) {
      val googlenews_id = resultSet.getObject[java.util.UUID]("id", classOf[UUID])
      val link = resultSet.getString("link")
      val articleText = getArticleText(link)
      val sentencesAndSentiments = extractSentiments(articleText)
      val sentences = sentencesAndSentiments.map(_._1)
      val sentiments = sentencesAndSentiments.map(_._2)
    }
  }

  override def updateTableWithFreshData(): Unit = {
    updateData()
    insertRecords()
  }

}
