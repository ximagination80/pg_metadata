package core

import java.io.File
import java.sql.{Connection, DriverManager}

import scala.util.{Failure, Success, Try}

case class CFGService() {
  val parser = new scopt.OptionParser[CFG]("DAO layer generator for postgres") {
    head( """
          Developer
          gmail: ximagination80@gmail.com
          skype: imagination80
          """)
    help("help")

    opt[String]("host") optional() action { (x, c) =>
      c.copy(host = x)
    } text """Host. Default localhost"""

    opt[Int]("port") required() action { (x, c) =>
      c.copy(port = x)
    } text """Database port"""

    opt[String]("schema") required() action { (x, c) =>
      c.copy(schema = x)
    }

    opt[String]("user") required() action { (x, c) =>
      c.copy(user = x)
    }

    opt[String]("password") required() action { (x, c) =>
      c.copy(password = x)
    }

    opt[Boolean]("debug") optional() valueName "true|false" action { (x, c) =>
      c.copy(debug = x)
    }

    checkConfig { c =>
      success
    }
  }

  def execute(args: Array[String], f: (CFG) => Unit) = {
    val parsed = parser.parse(args, CFG())
    parsed match {
      case Some(cfg) =>
        f(cfg)

      case None => // ignore
    }
  }
}

case class CFG(host: String = "localhost",
               port: Int = 0,
               schema: String = "",
               user: String = "",
               password: String = "",
               debug: Boolean = false) {

  def toSettings = AppSettings(debug)

  def createConnection(f: (Connection) => Unit) = Try {
    Class.forName("org.postgresql.Driver")
    DriverManager.getConnection(s"jdbc:postgresql://$host:$port/$schema", user, password)
  } match {
    case Success(c) =>
      try f(c) finally c.close()
    case Failure(e) =>
      e.printStackTrace()
  }
}

case class AppSettings(debug: Boolean){

  def onDebug(f: => Unit): Unit = if (debug) f

  def log(msg: String): Unit = onDebug {
    println(msg)
  }

  def logF(template: String, args: String*): Unit = onDebug {
    printf(template, args: _*)
  }
}
