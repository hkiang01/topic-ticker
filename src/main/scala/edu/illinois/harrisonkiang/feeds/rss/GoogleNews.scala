package edu.illinois.harrisonkiang.feeds.rss

import java.sql.{SQLException, Timestamp}
import java.text.SimpleDateFormat
import java.util.TimeZone

import edu.illinois.harrisonkiang.feeds.{Feed, Schema, SchemaCol}
import edu.illinois.harrisonkiang.util.TopicTickerLogger

import scala.collection.immutable
import scala.xml.{Elem, NodeSeq, XML}
import scalaj.http.{Http, HttpResponse}

case class GoogleNewsObj(guid: String, title: String, link: String, pubDate: Timestamp)

class GoogleNews extends Feed with TopicTickerLogger {

  override val tableName: String = "googlenews"

  val guidLength = 62
  val DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
  override val schema: Schema = Schema(Array(
    SchemaCol("guid", s"char($guidLength)"),
    SchemaCol("title", "text"),
    SchemaCol("link", "text"),
    SchemaCol("pubDate", "timestamp")
  ))
  override val uniqueCol: String = "link"

  var data: Seq[GoogleNewsObj] = Seq()

  override def createTableStatement: String = super.createTableStatement
  override def queryHeaderForInsertRecords: String = super.queryHeaderForInsertRecords

  override def insertRecords(): Unit = {
    ensureTableExists()

    var myConnection = connection

    val nonConflictingInsertQuery = queryHeaderForInsertRecords.concat(
      " ON CONFLICT DO NOTHING"
    )

    connection.setAutoCommit(false)
    val stmt = connection.prepareStatement(nonConflictingInsertQuery)

    data.foreach(datum => {
      stmt.setString(1, datum.guid)
      stmt.setString(2, datum.title)
      stmt.setString(3, datum.link)
      stmt.setTimestamp(4, datum.pubDate)
      stmt.addBatch()
    })

    logger.info(stmt.toString)

    stmt.executeBatch()
    data = null
  }

  override def dropTableStatement: String = super.dropTableStatement

  private def getRawResponse: HttpResponse[String] = Http("https://news.google.com/news/rss/?ned=us&hl=en")
    .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
    .asString
  private def getResponseBody: String = getRawResponse.body
  private def getResponseBodyAsXmlElem: Elem = XML.loadString(getResponseBody)
  private def getItemsAsNodeSeq: NodeSeq = getResponseBodyAsXmlElem \\ "item"

  private def getTimestamp(str: String): Timestamp = {
    val f  = new SimpleDateFormat(DATE_FORMAT)
    f.setTimeZone(TimeZone.getTimeZone("UTC"))
    val time = f.parse(str)
    new Timestamp(time.getTime)
  }

  def updateData(): Unit = {
    val newData = getItemsAsNodeSeq.map(nodeSeq => {
      GoogleNewsObj(
        (nodeSeq \ "guid").text,
        (nodeSeq \ "title").text,
        (nodeSeq  \ "link").text,
        getTimestamp((nodeSeq \ "pubDate").text))
    })
    this.data = newData
  }

  override def updateTableWithFreshData(): Unit = {
    updateData()
    insertRecords()
  }

}
