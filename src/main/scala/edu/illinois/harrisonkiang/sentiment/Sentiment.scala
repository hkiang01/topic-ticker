package edu.illinois.harrisonkiang.sentiment

import edu.illinois.harrisonkiang.postgres.PostgresDBConnection

/**
  * Created by harrison.kiang on 7/10/17.
  *
  * @see <a href="https://github.com/shekhargulati/52-technologies-in-2016/blob/master/03-stanford-corenlp/README.md"></a>
  */
object Sentiment extends Enumeration {
  type Sentiment = Value
  val VERY_POSITIVE, POSITIVE,NEUTRAL, NEGATIVE, VERY_NEGATIVE = Value

  def toSentiment(sentiment: Int): Sentiment = sentiment match {
    case 0 => Sentiment.VERY_NEGATIVE
    case 1 => Sentiment.NEGATIVE
    case 2 => Sentiment.NEUTRAL
    case 3 => Sentiment.POSITIVE
    case 4 => Sentiment.VERY_POSITIVE
  }

  def createEnumStatement: String =
    "DO $$\nBEGIN\nIF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sentiment') THEN \n" +
      "CREATE TYPE sentiment AS ENUM (" +
      Sentiment.values.map(_.toString).toArray.mkString("\'", "\', \'", "\'") +
      ");\n" +
      "END IF;\nEND\n$$"

  def createSentimentEmum(): Unit = {
    new PostgresDBConnection().createConnection().createStatement().execute(createEnumStatement)
  }
}
