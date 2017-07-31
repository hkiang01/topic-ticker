package edu.illinois.harrisonkiang.sentiment

import java.sql.ResultSet
import java.util.UUID

import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicTickerLogger, TopicTickerTable}

case class GoogleNewsUUIDLink(googlenews_id: UUID, link: String)
case class GoogleNewsSentencesAndSentimentObj(googlenews_id: UUID, sentences: java.sql.Array, sentiment: java.sql.Array)

class GoogleNewsSentencesAndSentiment extends TopicTickerTable with TopicTickerLogger {

  // enables uuid_generate_v4
  connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")

  override val tableName: String = "googlenews_sentenceandsentiment"

  override val schema: Schema = Schema(Array(
    SchemaCol("id", "uuid DEFAULT uuid_generate_v4 ()"),
    SchemaCol("googlenews_id", "uuid"),
    SchemaCol("sentences", s"text[]"),
    SchemaCol("sentiment", s"double precision[]")
  ))
  override val uniqueCol: String = "id"

  var data: Seq[GoogleNewsSentencesAndSentimentObj] = Seq()

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
      stmt.setArray(3, datum.sentiment)
      stmt.addBatch()
    })

    logger.info(stmt.toString)

    stmt.executeBatch()
    data = null
  }

  def googleNewsIdsAndLinksWithoutTextAndSentiment(): ResultSet = {
    ensureTableExists()
    val stmt = connection.createStatement()
    val sql = s"select id, link from googlenews where id not in (select googlenews_id from $tableName)"
    stmt.executeQuery(sql)
  }

  def updateData(): Unit = {
    ensureTableExists()

    val resultSet = googleNewsIdsAndLinksWithoutTextAndSentiment()
    while(resultSet.next()) {
      val uuid = resultSet.getObject[java.util.UUID]("id", classOf[UUID])
      val link = resultSet.getString("link")
      val newElem = GoogleNewsUUIDLink(uuid, link)
      logger.info(newElem.toString)
    }
  }

  override def updateTableWithFreshData(): Unit = {
    updateData()
    insertRecords()
  }

}
