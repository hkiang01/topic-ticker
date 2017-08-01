package sentiment

import java.util.UUID

import edu.illinois.harrisonkiang.sentiment.{GoogleNewsSentencesAndSentiments, GoogleNewsSentencesAndSentimentsObj}
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

  ignore("insert records") {
    googleNewsSentencesAndSentiment.updateBatch(1)
    googleNewsSentencesAndSentiment.insertRecords()
    val rs = googleNewsSentencesAndSentiment.getRecords
    rs.next()

    val googleNewsSentencesAndSentimentObj = GoogleNewsSentencesAndSentimentsObj(
        rs.getObject("googlenews_id").asInstanceOf[UUID],
        rs.getArray("sentences"),
        rs.getArray("sentiments")
      )
    it("an inserted record should be obtainable") {
      googleNewsSentencesAndSentimentObj shouldBe a [GoogleNewsSentencesAndSentimentsObj]
    }
  }
}
