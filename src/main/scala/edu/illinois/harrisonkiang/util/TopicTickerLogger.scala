package edu.illinois.harrisonkiang.util

import org.apache.log4j.{LogManager, Logger}

trait TopicTickerLogger {
  val logger: Logger = LogManager.getLogger(this.getClass.toString)
}
