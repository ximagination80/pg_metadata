import core._

object Main {

  def main(args: Array[String]) {
    CFGService().execute(args, { implicit cfg =>
      cfg.createConnection { implicit c =>
        implicit val stg = cfg.toSettings

        val seq = PGMetadataCollector(cfg.schema).collect()
        Printer().printCollectedInfo(seq)
      }
    })
  }
}


