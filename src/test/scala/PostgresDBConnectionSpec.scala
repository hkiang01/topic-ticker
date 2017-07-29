import java.text.SimpleDateFormat

import org.scalatest.{FunSpec, Matchers}
import java.util.{Date, Properties}
import java.sql.DriverManager

/**
  * Created by harrison.kiang on 7/29/17.
  * @see <a href="https://stackoverflow.com/questions/3194589/how-can-i-connect-to-a-postgresql-database-in-scala"></a>
  */
class PostgresDBConnectionSpec extends FunSpec with Matchers {
  describe("connection to postgres database analyzer") {
    println("Postgres connector")

    val dbName = "topictickerdb"
    val url = s"jdbc:postgresql://localhost/$dbName"

    val props = new Properties()
    props.setProperty("user", "topictickeruser")
    props.setProperty("password", "topictickerpassword")
    props.setProperty("ssl", "true")

    val connection = DriverManager.getConnection(url, props)

    try {

      val currentDateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
      val currentDate = currentDateFormat.format(new Date)

      val statement = connection.createStatement()
      val rs = statement.executeQuery("SELECT current_date")
      rs.next()
      val resultSetDate = rs.getString("date")


      it("jdbc driver should connecto to topictickerdb as topictickeruser and execute a simple function") {
        resultSetDate should be (currentDate)
      }

    } catch {
      case e: Exception => {
        e.printStackTrace()
        fail()
      }
    } finally {
      connection.close()
    }
  }
}
