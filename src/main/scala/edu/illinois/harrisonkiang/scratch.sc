import edu.illinois.harrisonkiang.sentiment.Sentiment

val sentiments: Array[String] = Sentiment.values.map(_.toString).toArray
val createEnumStatement: String = "CREATE TYPE sentiment AS ENUM (" +
  sentiments.mkString("\'", "\', \'", "\'") +
  ")"
println(createEnumStatement)