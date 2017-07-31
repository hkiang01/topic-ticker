package sentiment

import java.util.UUID

import edu.illinois.harrisonkiang.sentiment.{GoogleNewsSentencesAndSentiment, GoogleNewsSentencesAndSentimentObj}
import edu.illinois.harrisonkiang.util.TopicTickerLogger
import org.scalatest.{FunSpec, Matchers}

class GoogleNewsTextAndSentimentSpec extends FunSpec with Matchers with TopicTickerLogger {

  val googleNewsSentencesAndSentiment = new GoogleNewsSentencesAndSentiment

  describe("sample feeds") {
    it("data should be empty at start") {
      googleNewsSentencesAndSentiment.data.isEmpty should be (true)
    }
  }

  ignore("data updated") {
    it("after calling a method in googleNews, data should be updated") {
      googleNewsSentencesAndSentiment.updateData()
      googleNewsSentencesAndSentiment.data.take(5).foreach(println)
      googleNewsSentencesAndSentiment.data.nonEmpty should be (true)
    }
  }

  describe("ensure table exists") {
    googleNewsSentencesAndSentiment.ensureTableExists()
    it("table should exist if it is ensured to exist") {
      googleNewsSentencesAndSentiment.tableExists should be (true)
    }
  }

  ignore("insert records") {
    googleNewsSentencesAndSentiment.updateData()
    googleNewsSentencesAndSentiment.insertRecords()
    val rs = googleNewsSentencesAndSentiment.getRecords
    rs.next()

    val googleNewsSentencesAndSentimentObj = GoogleNewsSentencesAndSentimentObj(
        rs.getObject("googlenews_id").asInstanceOf[UUID],
        rs.getArray("sentences"),
        rs.getArray("sentiment")
      )
    it("a record should be obtainable") {
      googleNewsSentencesAndSentimentObj shouldBe a [GoogleNewsSentencesAndSentimentObj]
    }
  }

  describe("google news ids to provide text and sentiment for") {
    logger.info("test")
    googleNewsSentencesAndSentiment.updateData()
    it("create table statement") {
      true should be (true)
    }
  }
}
