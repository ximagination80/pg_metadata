package test

import java.sql.{Connection, DriverManager}
import com.typesafe.config.ConfigFactory

trait PGConnectionUtils {
  Class.forName(driver)

  lazy val cfg = ConfigFactory.load().getConfig("pg")

  def host: String = cfg.getString("host")
  def port: Int = cfg.getInt("port")
  def user: String = cfg.getString("user")
  def password: String = cfg.getString("password")
  def database: String = cfg.getString("database")
  def driver: String = cfg.getString("driver")
  def url: String = s"jdbc:postgresql://$host:$port/$database"

  def url(schema:String=""): String =
    if (schema.nonEmpty) url+s"?currentSchema=$schema" else url

  def connect(schema: String = "", f: (Connection) => Unit) = {
    val c = DriverManager.getConnection(url(schema), user, password)
    try f(c) finally c.close()
  }
}
