package edu.illinois.harrisonkiang.feeds.rss

import java.sql.Connection
import java.time.{OffsetDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter

import edu.illinois.harrisonkiang.feeds.{Feed, Schema, SchemaCol}

import scala.collection.immutable
import scala.xml.{Elem, NodeSeq, XML}
import scalaj.http.{Http, HttpResponse}

case class GoogleNewsObj(guid: String, title: String, link: String, pubDate: OffsetDateTime)

class GoogleNews extends Feed {

  override val tableName: String = "googlenews"
  override val primaryKeyCol: String = "guid"

  val guidLength = 62
  override val schema: Schema = Schema(Array(
    SchemaCol("guid", s"char($guidLength)"),
    SchemaCol("title", "text"),
    SchemaCol("link", "text"),
    SchemaCol("pubDate", "timestamp with time zone")
  ))

  override def createTableStatement: String = super.createTableStatement
  override def dropTableStatement: String = super.dropTableStatement

  var data: Seq[GoogleNewsObj] = Seq()

  private def getRawResponse: HttpResponse[String] = Http("https://news.google.com/news/rss/?ned=us&hl=en")
    .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
    .asString
  private def getResponseBody: String = getRawResponse.body
  private def getResponseBodyAsXmlElem: Elem = XML.loadString(getResponseBody)
  private def getItemsAsNodeSeq: NodeSeq = getResponseBodyAsXmlElem \\ "item"

  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
  def getData: immutable.Seq[GoogleNewsObj] = {
    val newData = getItemsAsNodeSeq.map(nodeSeq => {
      GoogleNewsObj(
        (nodeSeq \ "guid").text,
        (nodeSeq \ "title").text,
        (nodeSeq  \ "link").text,
        ZonedDateTime.parse((nodeSeq \ "pubDate").text, dateTimeFormatter).toOffsetDateTime
      )
    })
    this.data = newData
    newData
  }



}
