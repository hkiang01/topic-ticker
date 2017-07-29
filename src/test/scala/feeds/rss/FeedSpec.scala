package feeds.rss

import edu.illinois.harrisonkiang.feeds.{Feed, Schema, SchemaCol}
import org.scalatest.{FunSpec, Matchers}

class FeedSpec extends FunSpec with Matchers {

  describe("feed connection") {
    class SampleFeed1 extends Feed {
      override val schema: Schema = Schema(Array(
        SchemaCol("col1", "type1")
      ))
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

  describe("multiple feed connections") {
    class SampleFeed1 extends Feed {
      override val schema: Schema = Schema(Array(
        SchemaCol("col1", "type1")
      ))
    }

    class SampleFeed2 extends Feed {
      override val schema: Schema = Schema(Array(
        SchemaCol("col2", "type2")
      ))
    }

    val sampleFeed1 = new SampleFeed1
    val sampleFeed2 = new SampleFeed2

    it("multiple feed connections should be valid") {
      val sampleFeedConnection1 = sampleFeed1.connection
      val sampleFeedConnection2 = sampleFeed2.connection
      sampleFeedConnection1.isValid(200) should be (true)
      sampleFeedConnection2.isValid(200) should be (true)
      sampleFeedConnection1.close()
      sampleFeedConnection2.close()
    }

    it("multiple feed connections should be closeable") {
      val sampleFeedConnection1 = sampleFeed1.connection
      val sampleFeedConnection2 = sampleFeed2.connection
      sampleFeedConnection1.close()
      sampleFeedConnection1.isClosed should be (true)
      sampleFeedConnection2.close()
      sampleFeedConnection2.isClosed should be (true)
    }


  }
}
