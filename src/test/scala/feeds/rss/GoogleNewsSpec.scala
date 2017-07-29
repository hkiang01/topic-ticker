package feeds.rss

import edu.illinois.harrisonkiang.feeds.rss.GoogleNews
import org.scalatest.{FunSpec, Matchers}
import scala.reflect.runtime._

class GoogleNewsSpec extends FunSpec with Matchers {

  val googleNews = new GoogleNews

  describe("state of the feed") {

    it("guid length should match known value") {
      val validGuidLengthCheck = googleNews.getData.forall(_.guid.length == googleNews.guidLength)
      validGuidLengthCheck should be(true)
    }
  }
}
