package feeds.rss

import edu.illinois.harrisonkiang.feeds.rss.GoogleNews
import edu.illinois.harrisonkiang.util.{Schema, SchemaCol, TopicTickerTable}
import org.scalatest.{FunSpec, Matchers}

class TopicTickerTableSpec extends FunSpec with Matchers {

  describe("single feed connection of a given type") {
    class SampleFeed1 extends TopicTickerTable {
      override val tableName: String = "samplefeed1"
      override val createTableStatement: String = ""
      override val dropTableStatement: String = ""
      override val schema: Schema = Schema(Array(
        SchemaCol("col1", "type1")
      ))
      override val uniqueCol: String = ""
      override def updateTableWithFreshData(): Unit = {}
      override def insertRecords(): Unit = {}
    }

    val sampleFeed1 = new SampleFeed1

    it("sample feed connection should be valid") {
      val sampleFeedConnection1 = sampleFeed1.connection
      sampleFeedConnection1.isValid(200) should be (true)
      sampleFeedConnection1.isClosed should be (false)
    }

    it("sample feed connection should be closeable") {
      val sampleFeedConnection1 = sampleFeed1.connection
      sampleFeedConnection1.close()
      sampleFeedConnection1.isClosed should be (true)
    }
  }

  describe("multiple feed connections of different types") {
    class SampleFeed1 extends TopicTickerTable {
      override val tableName: String = "samplefeed1"
      override val createTableStatement: String = ""
      override val dropTableStatement: String = ""
      override val schema: Schema = Schema(Array(
        SchemaCol("col1", "type1")
      ))
      override val uniqueCol: String = ""
      override def updateTableWithFreshData(): Unit = {}
      override def insertRecords(): Unit = {}
    }

    class SampleFeed2 extends TopicTickerTable {
      override val tableName: String = "samplefeed2"
      override val createTableStatement: String = ""
      override val dropTableStatement: String = ""
      override val schema: Schema = Schema(Array(
        SchemaCol("col2", "type2")
      ))
      override val uniqueCol: String = ""
      override def updateTableWithFreshData(): Unit = {}
      override def insertRecords(): Unit = {}
    }

    val sampleFeed1 = new SampleFeed1
    val sampleFeed2 = new SampleFeed2

    it("multiple feed connections of different types should be valid") {
      val sampleFeedConnection1 = sampleFeed1.connection
      val sampleFeedConnection2 = sampleFeed2.connection
      sampleFeedConnection1.isValid(500) should be (true)
      sampleFeedConnection2.isValid(500) should be (true)
      sampleFeedConnection1.close()
      sampleFeedConnection2.close()
    }

    it("multiple feed connections of different types should be closeable") {
      val sampleFeedConnection1 = sampleFeed1.connection
      val sampleFeedConnection2 = sampleFeed2.connection
      sampleFeedConnection1.close()
      sampleFeedConnection1.isClosed should be (true)
      sampleFeedConnection2.close()
      sampleFeedConnection2.isClosed should be (true)
    }
  }

  describe("multiple feed instances of same type") {
    class SampleFeed1 extends TopicTickerTable {
      override val tableName: String = "samplefeed1"
      override val createTableStatement: String = ""
      override val dropTableStatement: String = ""
      override val schema: Schema = Schema(Array(
        SchemaCol("col1", "type1")
      ))
      override val uniqueCol: String = ""
      override def updateTableWithFreshData(): Unit = {}
      override def insertRecords(): Unit = {}
    }

    val sampleFeed1 = new SampleFeed1
    val anotherSampleFeed1 = new SampleFeed1

    it("multiple feed instances of same type should be able to have valid connections") {
      val sampleFeedConnection1 = sampleFeed1.connection
      val anotherSampleFeedConnection1 = anotherSampleFeed1.connection
      sampleFeedConnection1.isValid(500) should be (true)
      anotherSampleFeedConnection1.isValid(500) should be (true)
      sampleFeedConnection1.close()
      anotherSampleFeedConnection1.close()
    }
  }

  // WARNING: RUNNING THIS TEST MAY ERASE DATA!!!
  ignore("tables need to be created and deleted") {
    val googleNews = new GoogleNews
    googleNews.createTable()
    val dbm = googleNews.connection.getMetaData
    val rs = dbm.getTables(null, null, googleNews.tableName, null)
    val record = rs.next
    println(record)
    if (rs.next) System.out.println("Table exists")
    else System.out.println("Table does not exist")
    googleNews.dropTable()
  }
}
