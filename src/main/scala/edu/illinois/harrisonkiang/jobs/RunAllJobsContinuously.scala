package edu.illinois.harrisonkiang.jobs

import edu.illinois.harrisonkiang.jobs.ingestion.IngestGoogleNews
import edu.illinois.harrisonkiang.jobs.transformation.{TransformGoogleNews, UpdateGoogleNewsEntities}

object RunAllJobsContinuously {
  def main(args: Array[String]): Unit = {
    while(true) {
      IngestGoogleNews.execute()
      TransformGoogleNews.execute()
      UpdateGoogleNewsEntities.execute()
    }
  }
}
