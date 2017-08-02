package sentiment

import java.util.UUID

import edu.illinois.harrisonkiang.sentiment.Sentiment.Sentiment
import edu.illinois.harrisonkiang.sentiment.{GoogleNewsSentenceAndSentimentObj, GoogleNewsSentencesAndSentiments, GoogleNewsSentencesAndSentimentsObj, Sentiment}
import edu.illinois.harrisonkiang.util.TopicTickerLogger
import org.scalatest.{FunSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

class GoogleNewsSentencesAndSentimentSpec extends FunSpec with Matchers with TopicTickerLogger {

  val googleNewsSentencesAndSentiment = new GoogleNewsSentencesAndSentiments()

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

  describe("insert single record") {
    val randomUUID = UUID.randomUUID()
    googleNewsSentencesAndSentiment.data = ArrayBuffer(GoogleNewsSentenceAndSentimentObj(
      randomUUID,
      "",
      Sentiment.VERY_POSITIVE))
    googleNewsSentencesAndSentiment.insertRecords()
    val rs = googleNewsSentencesAndSentiment.getRecords(forceOpenConnection = true)
    rs.next()

    rs.getObject("id").asInstanceOf[UUID] should be (randomUUID)

    val googleNewsSentenceAndSentimentObj = GoogleNewsSentenceAndSentimentObj(
      rs.getObject("googlenews_id").asInstanceOf[UUID],
      rs.getString("sentence"),
      {
        val sentimentString = rs.getString("sentiment").trim
        Sentiment.values.find(_.toString == sentimentString).orNull
      }
    )
    it("an inserted record should be obtainable") {
      googleNewsSentenceAndSentimentObj.googlenews_id should not be (null)
      googleNewsSentenceAndSentimentObj.sentence should not be (null)
      googleNewsSentenceAndSentimentObj.sentiment should not be (null)
      googleNewsSentenceAndSentimentObj shouldBe a [GoogleNewsSentenceAndSentimentObj]
    }
  }

//  ignore("insert records") {
//    googleNewsSentencesAndSentiment.updateBatch()
//    googleNewsSentencesAndSentiment.insertRecords()
//    val rs = googleNewsSentencesAndSentiment.getRecords(forceOpenConnection = true)
//    rs.next()
//
//    val googleNewsSentencesAndSentimentObj = GoogleNewsSentencesAndSentimentsObj(
//        rs.getObject("googlenews_id").asInstanceOf[UUID],
//        rs.getArray("sentence"),
//        rs.getArray("sentiment")
//      )
//    it("an inserted record should be obtainable") {
//      googleNewsSentencesAndSentimentObj shouldBe a [GoogleNewsSentencesAndSentimentsObj]
//    }
//  }
}
