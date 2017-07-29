package edu.illinois.harrisonkiang.feeds.rss

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import edu.illinois.harrisonkiang.feeds.{Feed, Schema, SchemaCol}

import scala.collection.immutable
import scala.xml.{Elem, NodeSeq, XML}
import scalaj.http.{Http, HttpResponse}

case class GoogleNewsObj(guid: String, title: String, link: String, localDateTime: LocalDateTime)

class GoogleNews extends Feed {

  val guidLength = 62

  override val schema: Schema = Schema(Array(
    SchemaCol("guid", s"character($guidLength)"),
    SchemaCol("title", "text"),
    SchemaCol("link", "text")//,
//    SchemaCol("pubDate", )
  ))

  private def getRawResponse: HttpResponse[String] = Http("https://news.google.com/news/rss/?ned=us&hl=en")
    .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
    .asString

  def getResponseBody: String = getRawResponse.body

  def getResponseBodyAsXmlElem: Elem = XML.loadString(getResponseBody)

  def getItemsAsNodeSeq: NodeSeq = getResponseBodyAsXmlElem \\ "item"

  def getData: immutable.Seq[GoogleNewsObj] = getItemsAsNodeSeq.map(nodeSeq => {
    GoogleNewsObj( (nodeSeq \ "guid").text,
      (nodeSeq \ "title").text,
      (nodeSeq  \ "link").text,
      LocalDateTime.parse((nodeSeq \ "pubDate").text, DateTimeFormatter.RFC_1123_DATE_TIME)
    )
  })



}
