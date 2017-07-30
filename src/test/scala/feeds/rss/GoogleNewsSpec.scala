package feeds.rss

import edu.illinois.harrisonkiang.feeds.rss.GoogleNews
import org.scalatest.{FunSpec, Matchers}

class GoogleNewsSpec extends FunSpec with Matchers {

  val googleNews = new GoogleNews

  describe("sample feeds") {
    it("data should be empty at start") {
      googleNews.data.isEmpty should be (true)
    }
  }

  describe("state of the feed") {
    it("guid length should match known value") {
      val validGuidLengthCheck = googleNews.getData.forall(_.guid.length == googleNews.guidLength)
      validGuidLengthCheck should be(true)
    }
  }

  describe("data updated") {
    it("after calling a method in googleNews, data should be updated") {
      googleNews.getData
      googleNews.data.take(5).foreach(println)
      googleNews.data.nonEmpty should be (true)
    }
  }
}
