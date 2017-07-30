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
  val primaryKeyCol: String

  def connection: Connection = PostgresDBConnection.createConnection()

  def createTableStatement: String = {
    s"CREATE TABLE ${tableName.toLowerCase()} ("+
      schema.schemaCols.map(schemaCol => {
        s"${schemaCol.colName.toLowerCase()} ${schemaCol.colType.toUpperCase()}"
      }).mkString(", ") + s", PRIMARY KEY ( $primaryKeyCol ))"
  }

  def createTable(): Unit = {
    val stmt = connection.createStatement()
    stmt.execute(createTableStatement)
  }

  def dropTableStatement: String = s"DROP TABLE ${tableName.toUpperCase()}"
  def dropTable(): Unit = {
    val stmt = connection.createStatement()
    stmt.execute(dropTableStatement)
  }

  def closeConnection(): Unit = {
    connection.close()
  }
}
