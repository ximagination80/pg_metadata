package console

import java.sql.{Connection, DriverManager}

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

    opt[String]("database") required() action { (x, c) =>
      c.copy(database = x)
    }

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
               database: String = "",
               schema: String = "",
               user: String = "",
               password: String = "",
               debug: Boolean = false) {

  def toSettings = AppSettings(debug)

  def createConnection(f: (Connection) => Unit) = {
    Class.forName("org.postgresql.Driver")
    val c = DriverManager.getConnection(s"jdbc:postgresql://$host:$port/$database", user, password)
    try f(c) finally c.close()
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
