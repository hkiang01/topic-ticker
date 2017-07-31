package edu.illinois.harrisonkiang.sentiment

import edu.illinois.harrisonkiang.postgres.PostgresDBConnection

/**
  * Created by harrison.kiang on 7/10/17.
  *
  * @see <a href="https://github.com/shekhargulati/52-technologies-in-2016/blob/master/03-stanford-corenlp/README.md"></a>
  */
object Sentiment extends Enumeration {

  val sentiments: Array[String] = Sentiment.values.map(_.toString).toArray
  val createEnumStatement: String = "CREATE TYPE sentiment AS ENUM (" +
    sentiments.mkString("\'", "\', \'", "\'") +
    ")"
  new PostgresDBConnection().createConnection().createStatement().execute(createEnumStatement)

  type Sentiment = Value
  val VERY_POSITIVE, POSITIVE,NEUTRAL, NEGATIVE, VERY_NEGATIVE = Value

  def toSentiment(sentiment: Int): Sentiment = sentiment match {
    case 0 => Sentiment.VERY_NEGATIVE
    case 1 => Sentiment.NEGATIVE
    case 2 => Sentiment.NEUTRAL
    case 3 => Sentiment.POSITIVE
    case 4 => Sentiment.VERY_POSITIVE
  }
}
