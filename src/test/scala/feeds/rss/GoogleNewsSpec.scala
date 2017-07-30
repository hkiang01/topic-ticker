package feeds.rss

import edu.illinois.harrisonkiang.feeds.rss.{GoogleNews, GoogleNewsObj}
import org.scalatest.{FunSpec, Matchers}

class GoogleNewsSpec extends FunSpec with Matchers {

  val googleNews = new GoogleNews

  describe("sample feeds") {
    it("data should be empty at start") {
      googleNews.data should be (null)
    }
  }

  describe("state of the feed") {
    it("guid length should match known value") {
      googleNews.updateData()
      val validGuidLengthCheck = googleNews.data.forall(_.guid.length == googleNews.guidLength)
      validGuidLengthCheck should be(true)
    }
  }

  describe("data updated") {
    it("after calling a method in googleNews, data should be updated") {
      googleNews.updateData()
      googleNews.data.take(5).foreach(println)
      googleNews.data.nonEmpty should be (true)
    }
  }

  describe("ensure table exists") {
    googleNews.ensureTableExists()
    it("table should exist if it is ensured to exist") {
      googleNews.tableExists should be (true)
    }
  }

  describe("insert records") {
    googleNews.updateData()
    googleNews.insertRecords()
    val rs = googleNews.getRecords
    rs.next()
    val googleNewsObj = GoogleNewsObj(
        rs.getString(1),
        rs.getString(2),
        rs.getString(3),
        rs.getTimestamp(4)
      )
    it("a record should be obtainable") {
      googleNewsObj shouldBe a [GoogleNewsObj]
    }
  }

  describe("create table statement") {
    println(googleNews.createTableStatement)
    it("create table statement") {
      true should be (true)
    }
  }
}