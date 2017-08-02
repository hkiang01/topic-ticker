package edu.illinois.harrisonkiang.sentiment

import java.util.UUID

case class GoogleNewsSentencesAndSentimentsObj(googlenews_id: UUID, sentences: java.sql.Array, sentiments: java.sql.Array)
