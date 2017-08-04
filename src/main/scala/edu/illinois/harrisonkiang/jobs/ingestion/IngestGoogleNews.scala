package edu.illinois.harrisonkiang.jobs.ingestion

import edu.illinois.harrisonkiang.feeds.rss.GoogleNews
import edu.illinois.harrisonkiang.util.TopicTickerLogger

object IngestGoogleNews extends TopicTickerLogger{
  def execute(): Unit = {
    logger.info("executing IngestGoogleNews...")
    val googleNews = new GoogleNews
    googleNews.updateTableWithFreshData()
    googleNews.connection.close()
    logger.info("executed IngestGoogleNews...")
  }
}
