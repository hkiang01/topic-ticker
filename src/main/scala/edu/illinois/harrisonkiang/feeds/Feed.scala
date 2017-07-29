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
  val schema: Schema

  val connection: Connection = PostgresDBConnection.createConnection()



}
