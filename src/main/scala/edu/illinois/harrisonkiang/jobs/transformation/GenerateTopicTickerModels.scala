package edu.illinois.harrisonkiang.jobs.transformation

import edu.illinois.harrisonkiang.topicmodel.GoogleNewsTopicModel

object GenerateTopicTickerModels {
  def main(args: Array[String]): Unit = {
    (new GoogleNewsTopicModel).updateTableWithFreshData()
  }
}
