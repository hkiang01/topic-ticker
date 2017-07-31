package edu.illinois.harrisonkiang.jobs.transformation

import edu.illinois.harrisonkiang.sentiment.GoogleNewsSentencesAndSentiments

object TransformGoogleNews {
  def main(args: Array[String]): Unit = {
    (new GoogleNewsSentencesAndSentiments).updateData()
  }
}
