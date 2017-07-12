package edu.illinois.harrisonkiang

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.google.gson.JsonParser
import edu.illinois.harrisonkiang.Sentiment.Sentiment
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{AutoDetectParser, ParseContext, Parser}
import org.apache.tika.sax.BodyContentHandler

import scalaj.http.{Http, HttpResponse}
import scala.xml.{Elem, NodeSeq, XML}
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import scala.collection.GenSeq

case class TitleLinkDateTime(guid: String, title: String, link: String, localDateTime: LocalDateTime)
case class SentenceSentiment(sentence: String, sentiment: Sentiment)
case class ArticleText(guid: String, localDateTime: LocalDateTime,
                       sentenceSentiments: GenSeq[SentenceSentiment],
                       url: String, method: String, articleText: String)
case class TimeTagsSentimentScore(localDateTime: LocalDateTime, tags: GenSeq[String], sentimentScore: Double)

/**
  * Created by harry on 7/8/17.
  */
object GetXml extends App {

  private val JUICER_PREPEND_URL = "https://juicer.herokuapp.com/api/article?url="

  private val TEXT_THRESHOLD = 150

  private val logger: Logger = LogManager.getLogger(this.getClass.toString)

  /**
    * Uses juicer to grab the article body of an article
    * @param urlString the URL to plug into juicer API
    * @return the body result of the juicer API call
    * @see <a href="https://juicer.herokuapp.com/">juicer</a>
    * WARNING: Does not work for all sites and document types, e.g., PDFs
    */
  private def getArticleTextWithJuicer(urlString: String): String = {
    logger.info(s"Getting article text using juicer for urlString $urlString")
    try {
      val url = new URL(JUICER_PREPEND_URL + urlString)
      val bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()))
      val stringBuilder = new StringBuilder
      var line = ""
      line = bufferedReader.readLine()
      while(line != null) {
        stringBuilder.append(line)
        line = bufferedReader.readLine()
      }
      bufferedReader.close()
      val result = stringBuilder.toString()
      val jsonParser = new JsonParser
      val element = jsonParser.parse(result)
      val jsonObject = element.getAsJsonObject
      jsonObject.get("article").getAsJsonObject.get("body").getAsString
    } catch {
      case (e: Exception) => {
        logger.warn(s"Unable to get article text with juicer for url '$urlString'")
//        e.printStackTrace()
        ""
      }
    }
  }

  /**
    * A helper for using Apache Tika's Parser API
    * @param urlString the url to parse
    * @param parser the [[org.apache.tika.parser.AbstractParser]]
    * @param bodyContentHandler the [[BodyContentHandler]]
    * @param metadata the [[Metadata]]
    * @return the resultant parsed string
    * @see  <a href="https://tika.apache.org/1.15/examples.html#Parsing_using_the_Auto-Detect_Parser">https://tika.apache.org/1.14/examples.html#Parsing_using_the_Auto-Detect_Parser</a>
    */
  private def tikaParserHelper(urlString: String, parser: Parser, bodyContentHandler: BodyContentHandler, metadata: Metadata, parseContext: ParseContext): String = {
    try {
      val url = new URL(urlString)
      val inputStream = url.openStream()
      parser.parse(inputStream, bodyContentHandler, metadata, parseContext)
      inputStream.close()
      bodyContentHandler.toString
    } catch {
      case e: Exception => {
        logger.warn(s"Unable to get article text with tika for url '$urlString'")
//        e.printStackTrace()
        ""
      }
    }
  }

  private def getArticleTextWithTikaAutoDetectParser(urlString: String): String = {
    logger.info(s"Getting article text using Tika Auto-Detect Parser for urlString $urlString")
    tikaParserHelper(urlString, new AutoDetectParser, new BodyContentHandler, new Metadata, new ParseContext)
  }

  // get the xml content using scalaj-http
  val response: HttpResponse[String] = Http("https://news.google.com/news/rss/?ned=us&hl=en")
    .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
    .asString

  val xmlString: String = response.body

  // convert the 'String' to a 'scala.xml.Elem'
  val xml: Elem = XML.loadString(xmlString)

  val items: NodeSeq = xml \\ "item"
  val titleLinkPubDateNodes = for {
    i <- items
    guid <- i \ "guid"
    title <- i \ "title"
    link <- i \ "link"
    pubDate <- i \ "pubDate"
  } yield (guid, title, link, pubDate)
  val titleLinkPubDateText = titleLinkPubDateNodes.map( n => {
    val guid = n._1.text
    val title = n._2.text
    val link = n._3.text
    val timeString = n._4.text
    val timeLocalDateTime = LocalDateTime.parse(timeString, DateTimeFormatter.RFC_1123_DATE_TIME)
    TitleLinkDateTime(guid, title, link, timeLocalDateTime)
  })
//  titleLinkPubDateText.foreach(println)

  /**
    * With Juicer, else with [[AutoDetectParser]]
    */
  private def getArticleTextAndMethod(urlString: String): (String, String) = {
    logger.debug(s"Getting article text and method for urlString $urlString")
    val articleTextJuicer = getArticleTextWithJuicer(urlString)
    if(articleTextJuicer.length < TEXT_THRESHOLD) {
      val articleTextAutoDetectParser = getArticleTextWithTikaAutoDetectParser(urlString)
      if(articleTextAutoDetectParser.length < TEXT_THRESHOLD) {
        logger.error(s"Unable to get article text for urlString $urlString (length ${Math.max(articleTextJuicer.length, articleTextAutoDetectParser.length)})")
      }
      ("Apache Tika Auto-Detect Parser", articleTextAutoDetectParser)
    } else {
      ("juicer", articleTextJuicer)
    }
  }

  def getArticleText(urlString: String): String = {
    getArticleTextAndMethod(urlString)._2
  }

  val articleText = titleLinkPubDateText.par.map(elem => {
    val guid = elem.guid
    val url = elem.link
    val methodAndArticleText = getArticleTextAndMethod(url)
    val method = methodAndArticleText._1
    val cleansedArticleText = methodAndArticleText._2.replaceAll("\\s+", " ")
    if(cleansedArticleText.nonEmpty) {
      val sentenceSentiments = {
        val results = SentimentAnalyzer.extractSentiments(cleansedArticleText)
        results.map(result => SentenceSentiment(result._1, result._2))
      }
      ArticleText(elem.guid, elem.localDateTime, sentenceSentiments, elem.link, method, cleansedArticleText)
    } else {
      null
    }
  }).filter(_ != null)

  articleText.foreach(println)
}
