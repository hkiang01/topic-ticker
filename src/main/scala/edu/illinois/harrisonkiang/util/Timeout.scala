package edu.illinois.harrisonkiang.util

import akka.actor.ActorSystem
import akka.pattern.after

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.FiniteDuration

/**
  * https://nami.me/2015/01/20/scala-futures-with-timeout/
  */
trait Timeout {
  implicit class FutureExtensions[T](f: Future[T]) {
    def withTimeout(timeout: => Throwable)(implicit duration: FiniteDuration, system: ActorSystem): Future[T] = {
      Future firstCompletedOf Seq(f, after(duration, system.scheduler)(Future.failed(timeout)))
    }
  }

}
