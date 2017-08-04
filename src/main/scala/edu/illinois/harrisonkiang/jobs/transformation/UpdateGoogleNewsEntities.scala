package edu.illinois.harrisonkiang.jobs.transformation

import edu.illinois.harrisonkiang.entityextraction.GoogleNewsArticleEntities
import edu.illinois.harrisonkiang.util.TopicTickerLogger

object UpdateGoogleNewsEntities extends TopicTickerLogger{
  def execute(): Unit = {
    logger.info("executing UpdateGoogleNewsEntities...")
    val googleNewsArticleEntities = new GoogleNewsArticleEntities
    googleNewsArticleEntities.updateData()
    googleNewsArticleEntities.connection.close()
    logger.info("executed UpdateGoogleNewsEntities")
  }
}
