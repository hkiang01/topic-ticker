package edu.illinois.harrisonkiang.postgres

import java.sql.{Connection, DriverManager}
import java.util.Properties

object PostgresDBConnection {

  private val defaultUrl = "jdbc:postgresql://localhost/"

  private val defaultDB = "topictickerdb"
  private val defaultUser = "topictickeruser"
  private val defaultPassword = "topictickerpassword"

  def createConnection(url: String = defaultUrl, db: String = defaultDB, user: String = defaultUser, password: String = defaultPassword): Connection = {
    val fullUrl = url.concat(db)
    val props = new Properties()
    props.setProperty("user", user)
    props.setProperty("password", password)
    props.setProperty("ssl", "true")

    DriverManager.getConnection(fullUrl, props)
  }

}
