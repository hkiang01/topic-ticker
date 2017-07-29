import java.io.{FileReader, IOException, StringReader}
import java.util

import edu.illinois.harrisonkiang.sentiment.SentimentAnalyzer
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.process.CoreLabelTokenFactory
import edu.stanford.nlp.process.DocumentPreprocessor
import edu.stanford.nlp.process.PTBTokenizer

import scala.collection.JavaConverters._
import scala.util.Try

val str = "Another ex-Golden Stater, Paul Stankowski from Oxnard, is contending\nfor a berth on the U.S. Ryder Cup team after winning his first PGA Tour\nevent last year and staying within three strokes of the lead through\nthree rounds of last month's U.S. Open. H.J. Heinz Company said it\ncompleted the sale of its Ore-Ida frozen-food business catering to the\nservice industry to McCain Foods Ltd. for about $500 million.\nIt's the first group action of its kind in Britain and one of\nonly a handful of lawsuits against tobacco companies outside the\nU.S. A Paris lawyer last year sued France's Seita SA on behalf of\ntwo cancer-stricken smokers. Japan Tobacco Inc. faces a suit from\nfive smokers who accuse the government-owned company of hooking\nthem on an addictive product."

val dp = new DocumentPreprocessor(new StringReader(str))
dp.iterator().asScala.foreach(elem => {
  val str = elem.toArray().mkString(" ")
  println("sentence\t" + str)
  println("sentiment\t" + SentimentAnalyzer.mainSentiment(str))
})
