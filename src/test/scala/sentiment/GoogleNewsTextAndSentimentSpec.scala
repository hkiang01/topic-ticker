package sentiment

import java.util.UUID

import edu.illinois.harrisonkiang.sentiment.{GoogleNewsSentencesAndSentiments, GoogleNewsSentencesAndSentimentsObj, Sentiment}
import edu.illinois.harrisonkiang.util.TopicTickerLogger
import org.scalatest.{FunSpec, Matchers}

class GoogleNewsTextAndSentimentSpec extends FunSpec with Matchers with TopicTickerLogger {

  val googleNewsSentencesAndSentiment = new GoogleNewsSentencesAndSentiments

  describe("sample feeds") {
    it("data should be empty at start") {
      googleNewsSentencesAndSentiment.data.isEmpty should be (true)
    }
  }

  describe("ensure table exists") {
    googleNewsSentencesAndSentiment.ensureTableExists()
    it("table should exist if it is ensured to exist") {
      googleNewsSentencesAndSentiment.tableExists should be (true)
    }
  }

  ignore("data updated") {
    it("after calling a method in googleNews, data should be updated") {
      googleNewsSentencesAndSentiment.updateData()
      googleNewsSentencesAndSentiment.data.take(5).foreach(println)
      googleNewsSentencesAndSentiment.data.nonEmpty should be (true)
    }
  }

  ignore("update batch and insert records") {
    googleNewsSentencesAndSentiment.updateBatch()
    googleNewsSentencesAndSentiment.insertRecords(forceOpenConnection = true)
    val rs = googleNewsSentencesAndSentiment.getRecords(forceOpenConnection = true)
    rs.next()

    val googleNewsSentencesAndSentimentObj = GoogleNewsSentencesAndSentimentsObj(
        rs.getObject("googlenews_id").asInstanceOf[UUID],
        rs.getArray("sentences"),
        rs.getArray("sentiments")
      )

    val sentences = googleNewsSentencesAndSentimentObj.sentences.getArray().asInstanceOf[Array[String]]
    logger.info(sentences)

    it("an inserted record should be obtainable") {
      googleNewsSentencesAndSentimentObj shouldBe a [GoogleNewsSentencesAndSentimentsObj]
    }
  }

  describe("get records") {
    val rs = googleNewsSentencesAndSentiment.getRecords(forceOpenConnection = true)
    rs.next()

    val googleNewsSentencesAndSentimentObj = GoogleNewsSentencesAndSentimentsObj(
      rs.getObject("googlenews_id").asInstanceOf[UUID],
      rs.getArray("sentences"),
      rs.getArray("sentiments")
    )

    val sentences = googleNewsSentencesAndSentimentObj.sentences.getArray().asInstanceOf[Array[String]]
    logger.info(sentences.mkString("\n"))

    it("an inserted record should be obtainable") {
      googleNewsSentencesAndSentimentObj shouldBe a [GoogleNewsSentencesAndSentimentsObj]
    }
  }

  describe("create type sentiment as enum") {
    println("asdf")
    println(Sentiment.createEnumStatement)
    it("") {
      1 should be (1)
    }
  }
}
