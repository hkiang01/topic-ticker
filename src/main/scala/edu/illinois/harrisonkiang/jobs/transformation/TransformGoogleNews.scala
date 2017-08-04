package edu.illinois.harrisonkiang.jobs.transformation

import edu.illinois.harrisonkiang.sentiment.GoogleNewsSentencesAndSentiments
import edu.illinois.harrisonkiang.util.TopicTickerLogger

object TransformGoogleNews extends TopicTickerLogger{
  def execute(): Unit = {
    logger.info("executing TransformGoogleNews...")
    val googleNewsSentencesAndSentiments = new GoogleNewsSentencesAndSentiments
    googleNewsSentencesAndSentiments.updateData()
    googleNewsSentencesAndSentiments.connection.close()
    logger.info("executed TransformGoogleNews")
  }
}
