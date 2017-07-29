package edu.illinois.harrisonkiang.textextraction

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL

import com.google.gson.JsonParser
import edu.illinois.harrisonkiang.util.TopicTickerLogger

object Juicer extends TopicTickerLogger {

  private val JUICER_PREPEND_URL = "https://juicer.herokuapp.com/api/article?url="

  private def getStringFromBufferedReader(bufferedReader: BufferedReader): String = {
    val stringBuilder = new StringBuilder
    var line = ""
    line = bufferedReader.readLine()
    while(line != null) {
      stringBuilder.append(line)
      line = bufferedReader.readLine()
    }
    bufferedReader.close()
    stringBuilder.toString()
  }

    /**
    * Uses juicer to grab the article body of an article
    * @param urlString the URL to plug into juicer API
    * @return the body result of the juicer API call
    * @see <a href="https://juicer.herokuapp.com/">juicer</a>
    * WARNING: Does not work for all sites and document types, e.g., PDFs
    */
  def getArticleTextWithJuicer(urlString: String): String = {
    logger.info(s"Getting article text using juicer for urlString $urlString")
    try {
      val url = new URL(JUICER_PREPEND_URL + urlString)
      val bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()))
      val result = getStringFromBufferedReader(bufferedReader)
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

}
