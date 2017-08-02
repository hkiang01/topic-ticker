package edu.illinois.harrisonkiang.sentiment

import java.sql.{ResultSet, Statement}
import java.util.UUID

import edu.illinois.harrisonkiang.textextraction.TextExtraction
import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicTickerLogger, TopicTickerTable}

import scala.collection.mutable.ArrayBuffer

class GoogleNewsSentencesAndSentiments extends TopicTickerTable with TextExtraction with SentimentAnalyzer with TopicTickerLogger {

  // enables uuid_generate_v4
  connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")

  override val tableName: String = "googlenews_sentencesandsentiments"

  Sentiment.createSentimentEmum()

  override val schema: Schema = Schema(Array(
    SchemaCol("id", "uuid DEFAULT uuid_generate_v4 ()"),
    SchemaCol("googlenews_id", "uuid"),
    SchemaCol("sentence", s"text"),
    SchemaCol("sentiment", s"sentiment")
  ))
  override val uniqueConstraint: String = "googlenews_id"

  var data: ArrayBuffer[GoogleNewsSentenceAndSentimentObj] = ArrayBuffer()

  override def createTableStatement: String = super.createTableStatement
  override def queryHeaderForInsertRecords: String = {
    val nonIdCols = schema.schemaCols.filterNot(_.colName == "id")
    s"INSERT INTO ${tableName.toLowerCase()} " +
      s"(" + nonIdCols.map(_.colName).mkString(", ") + ") VALUES " +
      "(?, ?, CAST(? AS sentiment))"
  }
  override def dropTableStatement: String = super.dropTableStatement

  override def insertRecords(forceOpenConnection: Boolean = false): Unit = {
    ensureTableExists()

    val nonConflictingInsertQuery = queryHeaderForInsertRecords.concat(
      " ON CONFLICT DO NOTHING"
    )

    connection.setAutoCommit(false)
    val stmt = connection.prepareStatement(nonConflictingInsertQuery)

    var batchExecutionGoogleNewsIds: ArrayBuffer[UUID] = ArrayBuffer()

    data.foreach((datum: GoogleNewsSentenceAndSentimentObj) => {
      stmt.setObject(1, datum.googlenews_id)
      stmt.setString(2, datum.sentence)
      stmt.setString(3, datum.sentiment.toString)
      stmt.addBatch()
      batchExecutionGoogleNewsIds += datum.googlenews_id
    })
    logger.info(stmt.toString)

    // execute batch
    logger.info("executing batch")
    val batchExecutionResults: Array[Int] = stmt.executeBatch()
    logger.info("batchExecutionResults: " + batchExecutionResults.mkString(" "))
    batchExecutionResults.zipWithIndex.foreach(batchExecutionResult => {
      if(batchExecutionResult._1 == Statement.EXECUTE_FAILED) {
        logger.error(s"Execution failed: failed to insert GoogleNewsSentenceAndSentimentObj " +
          s"for googlenews_id ${batchExecutionGoogleNewsIds(batchExecutionResult._2)}")
      }
      else if(batchExecutionResult._1 == 0) {
        logger.warn(s"No records updated: failed to insert GoogleNewsSentenceAndSentimentObj " +
          s"for googlenews_id ${batchExecutionGoogleNewsIds(batchExecutionResult._2)}")
      } else {
        logger.info("update executed successfully")
      }
    })

    if(!forceOpenConnection) {
      stmt.close()
    }
    data = ArrayBuffer()
  }

  def googleNewsIdsAndLinksWithoutSentenceAndSentiment(numRecords: Int = Integer.MAX_VALUE): ResultSet = {
    ensureTableExists()
    val stmt = connection.createStatement()
    val sql = s"select id, link from googlenews where id not in (select googlenews_id from $tableName) LIMIT $numRecords"
    stmt.executeQuery(sql)
  }

//  private def ensureRecordExists(googlenews_id: UUID): Unit = {
//    ensureTableExists()
//    val stmt = connection.prepareStatement(s"SELECT googlenews_id FROM $tableName WHERE googlenews_id = ")
//    val stmt = connection.createStatement()
//  }

  def updateBatch(batchSize: Int = 1): Boolean = {
    ensureTableExists()
    val resultSet = googleNewsIdsAndLinksWithoutSentenceAndSentiment(batchSize)
    var batchesRemaining = false
    while(resultSet.next()) {
        batchesRemaining = true
        val googleNewsId = resultSet.getObject[java.util.UUID]("id", classOf[UUID])
        val link = resultSet.getString("link")
        logger.info(s"processing $link")

        val articleText = getArticleText(link)
        logger.info(s"articleText: ${articleText.take(200)}...")

        try {
          logger.info(s"running extractSentiments for googlenews_id $googleNewsId")
          val sentencesAndSentiments = extractSentiments(articleText)
          logger.info(s"extractSentiments finished for googlenews_id $googleNewsId")

          val sentences = sentencesAndSentiments.map(_._1)
//          val sentencesSqlArr = connection.createArrayOf("text", sentences.toArray)

          val sentiments = sentencesAndSentiments.map(_._2)
//          val sentimentsSqlArr = connection.createArrayOf("sentiment", sentiments.toArray)
          val newElems = sentences.zip(sentiments).map(sentenceAndSentiment => {
            GoogleNewsSentenceAndSentimentObj(googleNewsId, sentenceAndSentiment._1, sentenceAndSentiment._2)
          })

//          val newElem = GoogleNewsSentencesAndSentimentsObj(googleNewsId, sentencesSqlArr, sentimentsSqlArr)
          data = data ++ newElems
        } catch {
          case (oom: OutOfMemoryError) => {
            logger.warn(s"out of memory, queuing empty sentences and sentiments for $link")
            data = data ++ ArrayBuffer(GoogleNewsSentenceAndSentimentObj(googleNewsId, null, null))

          } case (e: Exception) => {
            e.printStackTrace()
            logger.warn(s"exception, queuing empty sentences and sentiments for $link")
            data = data ++ ArrayBuffer(GoogleNewsSentenceAndSentimentObj(googleNewsId, null, null))
          }
        }
    }
    insertRecords()
    batchesRemaining
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
