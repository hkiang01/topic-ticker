import java.sql.{DriverManager, ResultSet}
import java.text.SimpleDateFormat

import org.scalatest.{FunSpec, Matchers}
import java.util.{Calendar, Properties}
import java.sql.DriverManager
import com.typesafe.config.ConfigFactory

/**
  * Created by harrison.kiang on 7/10/17.
  * @see <a href="https://stackoverflow.com/questions/3194589/how-can-i-connect-to-a-postgresql-database-in-scala"></a>
  */
class PostgresDBConnectionSpec extends FunSpec with Matchers {
  describe("connection to postgres database analyzer") {
    println("Postgres connector")

    val dbName = "topictickerdb"
    val url = s"jdbc:postgresql://localhost/$dbName"

    val props = new Properties()
    props.setProperty("user", "harry")
    props.setProperty("password", "mypassword")
    props.setProperty("ssl", "true")

    val conn = DriverManager.getConnection(url, props)

    try {

      val now: Calendar = Calendar.getInstance()
      val currentDateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
      val currentDate = currentDateFormat.format(now)

      val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
      val rs = stm.executeQuery("CREATE current_date")

      rs should be (currentDate)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        fail()
      }
    } finally {
      conn.close()
    }
  }
}
