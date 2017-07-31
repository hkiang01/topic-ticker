package edu.illinois.harrisonkiang

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import edu.illinois.harrisonkiang.feeds.rss.GoogleNews
import edu.illinois.harrisonkiang.sentiment.Sentiment.Sentiment
import edu.illinois.harrisonkiang.sentiment.SentimentAnalyzer
import edu.illinois.harrisonkiang.textextraction._
import edu.illinois.harrisonkiang.util.TopicTickerLogger
import org.apache.tika.parser.AutoDetectParser

import scalaj.http.{Http, HttpResponse}
import scala.collection.GenSeq
import scala.xml.{Elem, NodeSeq, XML}

case class TitleLinkDateTime(guid: String, title: String, link: String, localDateTime: LocalDateTime)
case class SentenceSentiment(sentence: String, sentiment: Sentiment)
case class ArticleText(guid: String, localDateTime: LocalDateTime,
                       sentenceSentiments: GenSeq[SentenceSentiment],
                       url: String, method: String, articleText: String)
case class TimeTagsSentimentScore(localDateTime: LocalDateTime, tags: GenSeq[String], sentimentScore: Double)

/**
  * Created by harry on 7/8/17.
  */
object GetXml extends App with TextExtraction with TopicTickerLogger {


  // get the xml content using scalaj-http
  val googleNews = new GoogleNews

  val titleLinkPubDateText = {
    googleNews.updateData()
    googleNews.data
  }




//  val articleText = titleLinkPubDateText.par.map(elem => {
//    val guid = elem.guid
//    val url = elem.link
//    val methodAndArticleText = getArticleTextAndMethod(url)
//    val method = methodAndArticleText._1
//    val cleansedArticleText = methodAndArticleText._2
//    if(cleansedArticleText.nonEmpty) {
//      val sentenceSentiments = {
//        val results = SentimentAnalyzer.extractSentiments(cleansedArticleText)
//        results.map(result => SentenceSentiment(result._1, result._2))
//      }
//      ArticleText(elem.guid, elem.localDateTime, sentenceSentiments, elem.link, method, cleansedArticleText)
//    } else {
//      null
//    }
//  }).filter(_ != null)

}
