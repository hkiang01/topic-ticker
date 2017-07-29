package postgres

import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date

import edu.illinois.harrisonkiang.postgres.PostgresDBConnection
import org.scalatest.{FunSpec, Matchers}

/**
  * Created by harrison.kiang on 7/29/17.
  * @see <a href="https://stackoverflow.com/questions/3194589/how-can-i-connect-to-a-postgresql-database-in-scala"></a>
  */
class PostgresDBConnectionSpec extends FunSpec with Matchers {

  def getCurrentDateString: String = {
    val currentDateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    currentDateFormat.format(new Date)
  }

  def getCurrentPostgresDBDateString(connection: Connection): String = {
    val statement = connection.createStatement()
    val rs = statement.executeQuery("SELECT current_date")
    rs.next()
    rs.getString("date")
  }

  describe("connection to postgres database analyzer") {
    val connection = PostgresDBConnection.createConnection()
    try {
      val resultSetDate = getCurrentPostgresDBDateString(connection)
      val currentDate = getCurrentDateString
      it("jdbc driver should connect to topictickerdb as topictickeruser and execute a simple function") {
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
