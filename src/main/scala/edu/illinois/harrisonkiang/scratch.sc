import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.TimeZone

val raw = "Sat, 29 Jul 2017 23:46:04 PDT"
val DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"

val f = new SimpleDateFormat(DATE_FORMAT)
f.setTimeZone(TimeZone.getTimeZone("UTC"))
val time = f.parse(raw)

new Timestamp(time.getTime)