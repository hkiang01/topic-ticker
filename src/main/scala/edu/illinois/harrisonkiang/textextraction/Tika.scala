package edu.illinois.harrisonkiang.textextraction

import java.net.URL

import edu.illinois.harrisonkiang.util.TopicTickerLogger
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{AutoDetectParser, ParseContext, Parser}
import org.apache.tika.sax.BodyContentHandler

object Tika extends TopicTickerLogger {

  /**
    * A helper for using Apache Tika's Parser API
    * @param urlString the url to parse
    * @param parser the [[org.apache.tika.parser                                        .AbstractParser]]
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

  def getArticleTextWithTikaAutoDetectParser(urlString: String): String = {
    logger.info(s"Getting article text using Tika Auto-Detect Parser for urlString $urlString")
    tikaParserHelper(urlString, new AutoDetectParser, new BodyContentHandler, new Metadata, new ParseContext)
  }
}
