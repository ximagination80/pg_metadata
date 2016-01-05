package test

import java.io.StringReader
import java.sql.{Connection, DriverManager}
import java.util.Properties

trait PGConnectionUtils extends TestUtils{
  Class.forName(driver)

  case class DatabaseConfig(
    host:String,
    port:Int,
    user:String,
    password:String,
    database:String,
    driver:String = "org.postgresql.Driver"
  )

  def readConfig(): DatabaseConfig = {
    val configFile = resources ~> "reference.properties"

    val property = new Properties()
    property.load(new StringReader(configFile.read))

    DatabaseConfig(
      property.getProperty("host"),
      property.getProperty("port").toInt,
      property.getProperty("user"),
      property.getProperty("password"),
      property.getProperty("database")
    )
  }

  lazy val cfg:DatabaseConfig = readConfig()

  def host: String = cfg.host
  def port: Int = cfg.port
  def user: String = cfg.user
  def password: String = cfg.password
  def database: String = cfg.database
  def driver: String = cfg.driver
  def url: String = s"jdbc:postgresql://$host:$port/$database"

  def url(schema:String=""): String =
    if (schema.nonEmpty) url+s"?currentSchema=$schema" else url

  def connect(schema: String, f: (Connection) => Unit) = {
    val c = DriverManager.getConnection(url(schema), user, password)
    try f(c) finally c.close()
  }
}
