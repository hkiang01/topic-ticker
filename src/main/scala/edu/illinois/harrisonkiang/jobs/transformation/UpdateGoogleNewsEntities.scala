package edu.illinois.harrisonkiang.jobs.transformation

import edu.illinois.harrisonkiang.entityextraction.GoogleNewsArticleEntities

object UpdateGoogleNewsEntities {
  def main(args: Array[String]): Unit = {
    val googleNewsArticleEntities = new GoogleNewsArticleEntities
    googleNewsArticleEntities.updateData()
    googleNewsArticleEntities.connection.close()
  }
}
