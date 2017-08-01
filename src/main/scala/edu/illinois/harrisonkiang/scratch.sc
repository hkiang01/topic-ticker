//import scala.concurrent._
//import scala.concurrent.duration._
//import ExecutionContext.Implicits.global
//
//lazy val f = future {
//  Thread.sleep(2000);
//  0
//}
//
//var result: Int = 9
//try {
//  result = Await.result(f, 3 second)
//} catch {
//  case te: TimeoutException => {
//    println("timed out")
//  }
//  case e: Exception => {
//    println("exception!")
//  }
//} finally {
//  println(result)
//}
//

Array.empty.nonEmpty