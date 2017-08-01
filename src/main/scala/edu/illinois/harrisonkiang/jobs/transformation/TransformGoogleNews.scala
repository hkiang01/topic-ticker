package edu.illinois.harrisonkiang.jobs.transformation

import edu.illinois.harrisonkiang.sentiment.GoogleNewsSentencesAndSentiments

object TransformGoogleNews {
  def main(args: Array[String]): Unit = {
    val googleNewsSentencesAndSentiments = new GoogleNewsSentencesAndSentiments
    googleNewsSentencesAndSentiments.updateData()
    googleNewsSentencesAndSentiments.connection.close()
  }
}
