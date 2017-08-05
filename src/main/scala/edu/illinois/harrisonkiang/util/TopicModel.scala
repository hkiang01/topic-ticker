package edu.illinois.harrisonkiang.util

trait TopicModel {
  def getTopicModelForText(sentences: Array[String]): Array[(String, Double)] = {

    val words: Array[(String, Long)] = sentences.flatMap(_.split(" ")).map(line => (line, 1L))
    val numWords = words.map(_._2).sum
    val wordCounts: Map[String, Long] = words.groupBy(_._1).map(l => {
      (l._1,
        l._2.map(_._2).sum)})

    wordCounts.map(l => {
      (l._1, l._2 / numWords.toDouble)
    }).toArray
  }
}
