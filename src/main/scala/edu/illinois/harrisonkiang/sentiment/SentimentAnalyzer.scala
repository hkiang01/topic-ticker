package edu.illinois.harrisonkiang.sentiment

import java.util.Properties

import edu.illinois.harrisonkiang.sentiment.Sentiment.Sentiment
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.convert.wrapAll._

/**
  * Created by harrison.kiang on 7/10/17.
  * @see <a href="https://github.com/shekhargulati/52-technologies-in-2016/blob/master/03-stanford-corenlp/README.md"></a>
  */
trait SentimentAnalyzer {

  private val logger: Logger = LogManager.getLogger(this.getClass.toString)

  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  def mainSentiment(input: String, debugString: Option[String] = None): Sentiment = Option(input) match {
    case Some(text) if !text.isEmpty => {
      if(debugString.isDefined) {
        logger.debug(debugString.get)
      }
      extractSentiment(text)
    }
    case _ => throw new IllegalArgumentException("input can't be null or empty")
  }

  private def extractSentiment(text: String): Sentiment = {
    val (_, sentiment) = extractSentencesAndSentiments(text)
      .maxBy { case (sentence, _) => sentence.length }
    sentiment
  }

  def extractSentencesAndSentiments(text: String): List[(String, Sentiment)] = {
    logger.debug(s"running extractSentiments on ${text.take(1000)}...")
    val t0 = System.nanoTime()
    val annotation: Annotation = pipeline.process(text)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
    val results = sentences
      .map(sentence => (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
      .map { case (sentence, tree) => (sentence.toString,Sentiment.toSentiment(RNNCoreAnnotations.getPredictedClass(tree))) }
      .toList
    val t1 = System.nanoTime()
    logger.debug(s"${t1 - t0} ns elapsed for extractSentiments on ${text.take(100)}")
    results
  }

}