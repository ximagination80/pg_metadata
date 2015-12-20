package test

import java.sql.{Connection, DriverManager}

trait PGConnectionUtils {
  Class.forName(driver)

  // TODO extract to props
  def host: String = "localhost"
  def port: Int = 5432
  def user: String = "postgres"
  def password: String = "postgres"
  def database: String = "postgres"
  def driver: String = "org.postgresql.Driver"
  def url: String = s"jdbc:postgresql://$host:$port/$database"

  def url(schema:String=""): String =
    if (schema.nonEmpty) url+s"?currentSchema=$schema" else url

  def connect(schema: String = "", f: (Connection) => Unit) = {
    val c = DriverManager.getConnection(url(schema), user, password)
    try f(c) finally c.close()
  }
}
