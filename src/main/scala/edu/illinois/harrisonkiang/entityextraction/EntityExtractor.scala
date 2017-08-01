package edu.illinois.harrisonkiang.entityextraction

import java.util
import java.util.Properties

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation
import edu.stanford.nlp.coref.data.CorefChain

import collection.JavaConverters._
import edu.stanford.nlp.ling.CoreAnnotations.{NamedEntityTagAnnotation, SentencesAnnotation, TextAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap

import scala.collection.mutable

/**
  * @see <a href="https://github.com/clulab/processors/blob/master/corenlp/src/main/scala/org/clulab/processors/examples/ProcessorExample.scala">this example</a>
  */
class EntityExtractor {

  def extractEntities(text: String): Unit = {
    val props: Properties = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
    val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

    val document: Annotation = new Annotation(text)
    pipeline.annotate(document)

    val sentences= document.get(classOf[SentencesAnnotation]).asScala

    for (sentence: CoreMap <- sentences) {
      val tokens = sentence.get(classOf[TokensAnnotation]).asScala
      for (token <- tokens) {
        val word = token.get(classOf[TextAnnotation])
        val ne = token.get(classOf[NamedEntityTagAnnotation])
        println(word, ne)
      }
    }

//    val graph = document.get(classOf[CorefChainAnnotation]).asScala
//
//    for (chain: (Integer, CorefChain) <- graph) {
//      val corefChain: CorefChain = chain._2
//      corefChain.
//    }
  }

}
