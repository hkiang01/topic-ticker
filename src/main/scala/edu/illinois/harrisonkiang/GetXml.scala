package edu.illinois.harrisonkiang

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.google.gson.JsonParser
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{AutoDetectParser, ParseContext, Parser}
import org.apache.tika.sax.BodyContentHandler

import scalaj.http.{Http, HttpResponse}
import scala.xml.{Elem, NodeSeq, XML}

case class TitleLinkDateTime(guid: String, title: String, link: String, localDateTime: LocalDateTime)
case class ArticleText(guid: String, localDateTime: LocalDateTime, url: String, method: String, articleText: String)

/**
  * Created by harry on 7/8/17.
  */
object GetXml extends App {

  private val JUICER_PREPEND_URL = "https://juicer.herokuapp.com/api/article?url="

  private val TEXT_THRESHOLD = 150

  /**
    * Uses juicer to grab the article body of an article
    * @param urlString the URL to plug into juicer API
    * @return the body result of the juicer API call
    * @see <a href="https://juicer.herokuapp.com/">juicer</a>
    * WARNING: Does not work for all sites and document types, e.g., PDFs
    */
  private def getArticleTextWithJuicer(urlString: String): String = {
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
        e.printStackTrace()
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
    * @see  <a href="https://tika.apache.org/1.14/examples.html#Parsing_using_the_Auto-Detect_Parser">https://tika.apache.org/1.14/examples.html#Parsing_using_the_Auto-Detect_Parser</a>
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
        e.printStackTrace()
        ""
      }
    }
  }

  private def getArticleTextWithTikaAutoDetectParser(urlString: String): String = {
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
    val articleTextJuicer = getArticleTextWithJuicer(urlString)
    if(articleTextJuicer.length < TEXT_THRESHOLD) {
      val articleTextAutoDetectParser = getArticleTextWithTikaAutoDetectParser(urlString)
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
    val cleansedText = methodAndArticleText._2.replaceAll("\\s+", " ")
    ArticleText(elem.guid, elem.localDateTime, elem.link, method, cleansedText)
  })

  articleText.foreach(println)
}
