package edu.illinois.harrisonkiang.util

import java.sql.Connection

import edu.illinois.harrisonkiang.postgres.PostgresDBConnection

/**
  * Maps Scala types to Postgres types
  * @see <a href="https://www.postgresql.org/docs/9.6/static/datatype.html"></a>
  */
trait TopicTickerTable extends TopicTickerLogger{
  val tableName: String
  val schema: Schema
  val uniqueConstraint: String

  var connection: Connection = new PostgresDBConnection().createConnection()

  def createTableStatement: String = {
    s"CREATE TABLE ${tableName.toLowerCase()} ("+
      schema.schemaCols.map(schemaCol => {
        s"${schemaCol.colName.toLowerCase()} ${schemaCol.colType.toUpperCase()}"
      }).mkString(", ") + s", PRIMARY KEY (id), UNIQUE ($uniqueConstraint) )"
  }

  def createTable(): Unit = {
    ensureConnectionIsOpen()
    val stmt = connection.createStatement()
    logger.info("creating table")
    logger.info(createTableStatement)
    stmt.execute(createTableStatement)
    stmt.close()
  }

  def tableExists: Boolean = {
    val dbm = connection.getMetaData
    val rs = dbm.getTables(null, null, tableName, null)
    rs.next()
  }

  def ensureTableExists(): Unit = {
    if(!tableExists) {
      createTable()
    }
  }

  def getRecords(forceOpenConnection: Boolean = false): java.sql.ResultSet = {
    ensureConnectionIsOpen()
    val stmt = connection.createStatement()
    val sql = s"SELECT * FROM $tableName"
    val results = stmt.executeQuery(sql)
    if(!forceOpenConnection) {
      stmt.close()
    }
    results
  }

  def queryHeaderForInsertRecords: String = {
    val nonIdCols = schema.schemaCols.filterNot(_.colName == "id")
    s"INSERT INTO ${tableName.toLowerCase()} " +
      s"(" + nonIdCols.map(_.colName).mkString(", ") + ") VALUES " +
      "(" + ("?," * nonIdCols.length).dropRight(1) + ")"
  }

  def insertRecords(forceOpenConnection: Boolean = false): Unit

  def updateTableWithFreshData(): Unit

  def dropTableStatement: String = s"DROP TABLE ${tableName.toUpperCase()}"
  def dropTable(): Unit = {
    ensureConnectionIsOpen()
    val stmt = connection.createStatement()
    stmt.execute(dropTableStatement)
    stmt.close()
  }

  def ensureConnectionIsOpen(): Unit = {
    if(connection.isClosed) {
      connection = new PostgresDBConnection().createConnection()
    }
  }

  def closeConnection(): Unit = {
    connection.close()
  }
}
