import edu.illinois.harrisonkiang.sentiment.Sentiment

val sentiments: Array[String] = Sentiment.values.map(_.toString).toArray
val createEnumStatement: String =
  "DO $$\nBEGIN\nIF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sentiment') THEN \n" +
  "CREATE TYPE sentiment AS ENUM (" +
  sentiments.mkString("\'", "\', \'", "\'") +
  ");\n" +
  "END IF;\nEND\n$$"
println(createEnumStatement)