package edu.illinois.harrisonkiang.sentiment

import java.util.UUID

import edu.illinois.harrisonkiang.sentiment.Sentiment.Sentiment

case class GoogleNewsSentencesAndSentimentsObj(googlenews_id: UUID, sentences: java.sql.Array, sentiments: java.sql.Array)
case class GoogleNewsSentenceAndSentimentObj(googlenews_id: UUID, sentence: String, sentiment: Sentiment)
