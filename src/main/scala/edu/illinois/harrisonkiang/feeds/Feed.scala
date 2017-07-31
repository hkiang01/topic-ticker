package edu.illinois.harrisonkiang.feeds

import java.sql.Connection

import edu.illinois.harrisonkiang.postgres.PostgresDBConnection

case class SchemaCol(colName: String, colType: String)
case class Schema(schemaCols: Array[SchemaCol])

/**
  * Maps Scala types to Postgres types
  * @see <a href="https://www.postgresql.org/docs/9.6/static/datatype.html"></a>
  */
trait Feed {
  val tableName: String
  val schema: Schema
  val uniqueCol: String

  def connection: Connection = PostgresDBConnection.createConnection()

  def createTableStatement: String = {
    s"CREATE TABLE ${tableName.toLowerCase()} ("+
      schema.schemaCols.map(schemaCol => {
        s"${schemaCol.colName.toLowerCase()} ${schemaCol.colType.toUpperCase()}"
      }).mkString(", ") + s", PRIMARY KEY (id), UNIQUE ($uniqueCol) )"
  }

  def createTable(): Unit = {
    val stmt = connection.createStatement()
    stmt.execute(createTableStatement)
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

  def getRecords: java.sql.ResultSet = {
    val stmt = connection.createStatement()
    val sql = s"SELECT * FROM $tableName"
    stmt.executeQuery(sql)
  }

  def queryHeaderForInsertRecords: String = {
    val nonIdCols = schema.schemaCols.filterNot(_.colName == "id")
    s"INSERT INTO ${tableName.toLowerCase()} " +
      s"(" + nonIdCols.map(_.colName).mkString(", ") + ") VALUES " +
      "(" + ("?," * nonIdCols.length).dropRight(1) + ")"
  }

  def insertRecords(): Unit

  def updateTableWithFreshData(): Unit

  def dropTableStatement: String = s"DROP TABLE ${tableName.toUpperCase()}"
  def dropTable(): Unit = {
    val stmt = connection.createStatement()
    stmt.execute(dropTableStatement)
  }

  def closeConnection(): Unit = {
    connection.close()
  }
}
