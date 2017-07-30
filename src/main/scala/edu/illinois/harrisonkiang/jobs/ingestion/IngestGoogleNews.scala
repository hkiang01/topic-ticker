package edu.illinois.harrisonkiang.jobs.ingestion

import edu.illinois.harrisonkiang.feeds.rss.GoogleNews

object IngestGoogleNews {
  def main(args: Array[String]): Unit = {
    (new GoogleNews).updateTableWithFreshData()
  }
}
