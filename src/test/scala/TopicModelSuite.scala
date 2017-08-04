import edu.illinois.harrisonkiang.util.{TopicModel, TopicTickerLogger}
import org.scalatest.{FunSpec, Matchers}

class TopicModelSuite  extends FunSpec with Matchers with TopicModel with TopicTickerLogger{
  describe("topic model") {
    val topicModel = getTopicModelForText(Array(
      "This is a balloon. It is filled with Helium."))
    logger.info(topicModel)
    1 should be (1)
  }
}
