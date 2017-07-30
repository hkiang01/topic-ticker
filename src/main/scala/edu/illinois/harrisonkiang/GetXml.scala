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
object GetXml extends App with TopicTickerLogger {

  private val TEXT_THRESHOLD = 150

  // get the xml content using scalaj-http
  val googleNews = new GoogleNews

  val titleLinkPubDateText = googleNews.getData

  def cleanText(dirtyText: String): String = dirtyText.replaceAll("\\s+", " ")

  /**
    * With Juicer, else with [[AutoDetectParser]]
    */
  private def getArticleTextAndMethod(urlString: String): (String, String) = {
    logger.debug(s"Getting article text and method for urlString $urlString")
    val t0 = System.nanoTime()
    val articleTextAutoDetectParser = cleanText(Tika.getArticleTextWithTikaAutoDetectParser(urlString))
    val result = if(articleTextAutoDetectParser.length < TEXT_THRESHOLD) {
      val articleTextJuicer = cleanText(Juicer.getArticleTextWithJuicer(urlString))
      if(articleTextJuicer.length < TEXT_THRESHOLD) {
        logger.error(s"Unable to get article text for urlString $urlString (length ${Math.max(articleTextJuicer.length, articleTextAutoDetectParser.length)})")
        ("", "")
      } else {
        logger.debug(s"Got article text and method for urlString $urlString")
        ("juicer", articleTextJuicer)
      }
    } else {
      ("Apache Tika Auto-Detect Parser", articleTextAutoDetectParser)
    }
    val t1 = System.nanoTime()
    logger.debug(s"${t1 - t0} ns elapsed for getting article text and method for urlString $urlString: ${result._2.take(100)}")
    result
  }

  def getArticleText(urlString: String): String = {
    val result = getArticleTextAndMethod(urlString)._2
    if(result == null) "" else result
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
