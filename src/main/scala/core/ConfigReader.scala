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

    opt[File]("outDir") required() action { (x, c) =>
      c.copy(dir = x)
    }

    opt[Boolean]("debug") optional() valueName "true|false" action { (x, c) =>
      c.copy(debug = x)
    }

    checkConfig { c =>
      if (c.dir.isFile)
        failure("Provided directory is file")

      c.dir.mkdirs()
      if (!c.dir.exists())
        failure(s"Unable to create folder by path ${c.dir}")

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
               dir: File = new File("."),
               debug: Boolean = false) {

  def toSettings = AppSettings(dir, debug)

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

case class AppSettings(dir: File, debug: Boolean){
  def newFile(name: String) =
    new File(dir, name)

  def log(msg: String): Unit = if (debug) {
    println(msg)
  }

  def logF(template:String,args: String*): Unit = if (debug) {
    printf(template,args:_*)
  }
}
