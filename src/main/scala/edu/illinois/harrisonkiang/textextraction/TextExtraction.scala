package edu.illinois.harrisonkiang.textextraction

import org.apache.tika.parser.AutoDetectParser

trait TextExtraction extends Juicer with Tika {

  private val TEXT_THRESHOLD = 150

  private def cleanText(dirtyText: String): String = dirtyText.replaceAll("\\s+", " ")

  /**
    * With Juicer, else with [[AutoDetectParser]]
    */
  private def getArticleTextAndMethod(urlString: String): (String, String) = {
    logger.debug(s"Getting article text and method for urlString $urlString")
    val t0 = System.nanoTime()
    val articleTextAutoDetectParser = cleanText(getArticleTextWithTikaAutoDetectParser(urlString))
    val result = if(articleTextAutoDetectParser.length < TEXT_THRESHOLD) {
      val articleTextJuicer = cleanText(getArticleTextWithJuicer(urlString))
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
}
