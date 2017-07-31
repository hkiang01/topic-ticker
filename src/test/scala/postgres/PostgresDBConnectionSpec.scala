package postgres

import java.sql.{Connection, Statement}
import java.text.SimpleDateFormat
import java.util.Date

import edu.illinois.harrisonkiang.postgres.PostgresDBConnection
import org.scalatest.{FunSpec, Matchers}

/**
  * Created by harrison.kiang on 7/29/17.
  * @see <a href="https://stackoverflow.com/questions/3194589/how-can-i-connect-to-a-postgresql-database-in-scala"></a>
  */
class PostgresDBConnectionSpec extends FunSpec with Matchers {

  val postgresDBConnection = new PostgresDBConnection()

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
    val connection = postgresDBConnection.createConnection()
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

  describe("creation and deletion of a test table") {
    val testTableName = "testregistrationdb"
    var conn: Connection = null
    var stmt: Statement = null

    try {
      conn = postgresDBConnection.createConnection()
      stmt = conn.createStatement
      val sql = s"CREATE TABLE $testTableName " +
        "(id INTEGER not NULL, " +
        " first VARCHAR(255), " +
        " last VARCHAR(255), " +
        " age INTEGER, " +
        " PRIMARY KEY ( id ))"
      stmt.executeUpdate(sql)

      val dbm = conn.getMetaData
      val rs = dbm.getTables(null, null, testTableName, null)
      it("table should be found") {
        rs.next() should be (true)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        fail()
    } finally {
      try
          if (stmt != null) conn.close()
      catch {
        case e: Exception => e.printStackTrace()
      }

      try {
        if (conn != null) conn.close()
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }

    try {
      conn = postgresDBConnection.createConnection()
      stmt = conn.createStatement
      val sql = s"DROP TABLE $testTableName "
      stmt.executeUpdate(sql)
      val dbm = conn.getMetaData
      val rs = dbm.getTables(null, null, testTableName, null)
      it("table should not be found") {
        rs.next() should be (false)
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        fail()
      }
    } finally {
      try
          if (stmt != null) conn.close()
      catch {
        case e: Exception => e.printStackTrace()
      }
      try {
        if (conn != null) conn.close()
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }
  }
}
