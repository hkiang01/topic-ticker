package sentiment

import edu.illinois.harrisonkiang.sentiment.{Sentiment, SentimentAnalyzer}
import org.scalatest.{FunSpec, Matchers}

/**
  * Created by harrison.kiang on 7/10/17.
  * @see <a href="https://github.com/shekhargulati/52-technologies-in-2016/blob/master/03-stanford-corenlp/README.md"></a>
  */
class SentimentAnalyzerSpec extends FunSpec with Matchers {
  describe("sentiment analyzer") {
    it("should return POSITIVE when input has positive emotion") {
      val input = "Scala is a great general purpose language."
      val sentiment = SentimentAnalyzer.mainSentiment(input)
      sentiment should be(Sentiment.POSITIVE)
    }

    it("should return NEGATIVE when input has negative emotion") {
      val input = "Dhoni laments bowling, fielding errors in series loss"
      val sentiment = SentimentAnalyzer.mainSentiment(input)
      sentiment should be(Sentiment.NEGATIVE)
    }

    it("should return NEUTRAL when input has no emotion") {
      val input = "I am reading a book"
      val sentiment = SentimentAnalyzer.mainSentiment(input)
      sentiment should be(Sentiment.NEUTRAL)
    }
  }
}
