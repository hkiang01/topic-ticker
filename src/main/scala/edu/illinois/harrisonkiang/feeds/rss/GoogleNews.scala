package edu.illinois.harrisonkiang.feeds.rss

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.TimeZone

import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicTickerLogger, TopicTickerTable}

import scala.xml.{NodeSeq, XML}
import scalaj.http.Http

class GoogleNews extends TopicTickerTable with TopicTickerLogger {

  val rssUrls: Array[String] = Array(
    "https://news.google.com/news/rss/headlines/section/topic/WORLD?ned=us&hl=en",
    "https://news.google.com/news/rss/?ned=us&hl=en"
  )

  // enables uuid_generate_v4
  connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")

  override val tableName: String = "googlenews"

  val guidLength = 62
  val DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
  override val schema: Schema = Schema(Array(
    SchemaCol("id", "uuid DEFAULT uuid_generate_v4 ()"),
    SchemaCol("guid", s"char($guidLength)"),
    SchemaCol("title", "text"),
    SchemaCol("link", "text"),
    SchemaCol("pubdate", "timestamp")
  ))
  override val uniqueConstraint: String = "link"

  var data: Seq[GoogleNewsObj] = Seq()

  override def createTableStatement: String = super.createTableStatement
  override def queryHeaderForInsertRecords: String = super.queryHeaderForInsertRecords

  override def insertRecords(forceOpenConenction: Boolean = false): Unit = {
    ensureTableExists()

    val nonConflictingInsertQuery = queryHeaderForInsertRecords.concat(
      " ON CONFLICT DO NOTHING"
    )

//    connection.setAutoCommit(false)
    val stmt = connection.prepareStatement(nonConflictingInsertQuery)

    if(data != null) {
      data.foreach(println)
      data.foreach(datum => {
        stmt.setString(1, datum.guid)
        stmt.setString(2, datum.title)
        stmt.setString(3, datum.link)
        stmt.setTimestamp(4, datum.pubDate)
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

  def getRSSNodeSeq(rssUrl: String): NodeSeq = {
    val rawResponse = Http(rssUrl)
      .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
      .asString

    if(!rawResponse.isSuccess) {
      logger.error(s"${rawResponse.body}\n" +
        s"http request to $rssUrl was not successful!")
    }

    val xmlElem = XML.loadString(rawResponse.body)
    xmlElem \\ "item"
  }

  private def getTimestamp(str: String): Timestamp = {
    val f  = new SimpleDateFormat(DATE_FORMAT)
    f.setTimeZone(TimeZone.getTimeZone("UTC"))
    val time = f.parse(str)
    new Timestamp(time.getTime)
  }

  def updateData(forceOpenConnection: Boolean = false): Unit = {
    rssUrls.foreach(rssUrl => {
      val newData = getRSSNodeSeq(rssUrl).map(nodeSeq => {
        GoogleNewsObj(
          (nodeSeq \ "guid").text,
          (nodeSeq \ "title").text,
          (nodeSeq  \ "link").text,
          getTimestamp((nodeSeq \ "pubDate").text))
      })
      this.data = newData
      insertRecords(forceOpenConnection)
    })
  }

  override def updateTableWithFreshData(): Unit = {
    updateData()
  }
}
