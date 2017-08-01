package edu.illinois.harrisonkiang.entityextraction

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{NamedEntityTagAnnotation, SentencesAnnotation, TextAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * @see <a href="https://github.com/clulab/processors/blob/master/corenlp/src/main/scala/org/clulab/processors/examples/ProcessorExample.scala">this example</a>
  * @see <a href="https://sujitpal.blogspot.com/2014/09/coreference-resolution-with-stanford.html">this blog post</a>
  */
trait EntityExtractor {

  def extractEntities(text: String): Array[(String, String)] = {
    val props: Properties = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
    val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

    val document: Annotation = new Annotation(text)
    pipeline.annotate(document)

    val sentences= document.get(classOf[SentencesAnnotation]).asScala

    val textResult = sentences.flatMap(sentence => {
      val tokens = sentence.get(classOf[TokensAnnotation]).asScala

      // get pairs of (string, entity) of all entity types except 'O'
      val pairs: mutable.Seq[(String, String)] = tokens.map(token => {
        val word = token.get(classOf[TextAnnotation])
        val ne = token.get(classOf[NamedEntityTagAnnotation])
        (word, ne)
      }).filterNot(_._2 == "O").filterNot(_._2 == "")

      // coalesce adjacent entities of same type in a given sentence together
      var sentenceResults: mutable.Seq[(Array[String], String)] = mutable.Seq()
      var lastEntityTag = ""
      var temp: mutable.Buffer[String] = mutable.Buffer()
      for (pair <- pairs) {
        val entityTag = pair._2.trim
        if(entityTag == lastEntityTag || lastEntityTag == "") {
          lastEntityTag = entityTag
          temp += pair._1
        } else {
          val newElem: (Array[String], String) = (temp.toArray, lastEntityTag)
          sentenceResults = sentenceResults ++ Seq(newElem)
          temp.clear()
          temp += pair._1
          lastEntityTag = entityTag
        }
      }
      val newElem: (Array[String], String) = (temp.toArray, lastEntityTag)
      sentenceResults = sentenceResults ++ Seq(newElem)

      // format the output
      sentenceResults.map(sentenceResult => {
        (sentenceResult._1.mkString(" "), sentenceResult._2)
      })
    })

    textResult.filter(_._2 != "O").toArray
  }

}
