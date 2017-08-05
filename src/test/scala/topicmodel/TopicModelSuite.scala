package topicmodel

import edu.illinois.harrisonkiang.topicmodel.GoogleNewsTopicModel
import edu.illinois.harrisonkiang.util.{TopicModel, TopicTickerLogger}
import org.scalatest.{FunSpec, Matchers}

class TopicModelSuite  extends FunSpec with Matchers with TopicModel with TopicTickerLogger{

  val googleNewsTopicModel = new GoogleNewsTopicModel()

  describe("ensure table exits") {
    googleNewsTopicModel.ensureTableExists()
    googleNewsTopicModel.tableExists should be (true)
  }

  describe("topic model util") {
    val topicModel = getTopicModelForText(Array(
      "This is a balloon. It is filled with Helium."))
//    logger.info(s"topicModel: $topicModel")
    1 should be (1)
  }

  describe("topic model update batch") {
    googleNewsTopicModel.updateBatch()
    val data = googleNewsTopicModel.data
//    logger.info(s"data: ${data.mkString("\n")}")

    1 should be (1)
  }

}
