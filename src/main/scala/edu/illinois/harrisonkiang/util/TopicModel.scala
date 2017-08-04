package edu.illinois.harrisonkiang.util

import java.io.InputStream

import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

trait TopicModel {

//  val conf: SparkConf = new SparkConf().setAppName("Simple Application").setMaster("local[*]")
//  val sc: SparkContext = new SparkContext(conf)

  def getTopicModelForText(sentences: Array[String]): Array[(String, Double)] = {

    sentences.map(line => (line, 1.0)).toArray
//    val textFile = sc.textFile(text)
//    val words = textFile.flatMap(line => line.split(" "))
//    val counts = words.map(word => (word, 1))
//      .reduceByKey(_+_)
//    val numWords = counts.reduce((a, b) => ("", a._2 + b._2))._2.toDouble
//    val topicModel = counts.map(curr => (curr._1, curr._2 / numWords))
//    topicModel.collect()
  }
}
