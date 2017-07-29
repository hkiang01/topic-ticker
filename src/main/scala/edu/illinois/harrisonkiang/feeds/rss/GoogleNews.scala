package edu.illinois.harrisonkiang.feeds.rss

import scala.xml.{Elem, XML}
import scalaj.http.{Http, HttpResponse}

class GoogleNews {

  private def getRawResponse: HttpResponse[String] = Http("https://news.google.com/news/rss/?ned=us&hl=en")
    .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
    .asString

  def getResponseBody: String = getRawResponse.body

  def getResponseBodyAsXmlElem: Elem = XML.loadString(getResponseBody)



}
