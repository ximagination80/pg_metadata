package test

import java.io.File

import comparator._
import core.{AppSettings, PGMetadataCollector, TableDTO}
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST._
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.util.{Failure, Success, Try}

class SchemaCheck extends FunSuite
      with PGConnectionUtils
      with BeforeAndAfterAll
      with Comparison {

  implicit val settings = AppSettings(debug = false)

  val pg_schema = Seq("driver")

  override protected def beforeAll() = {
    pg_schema.foreach { e =>
      val fwy = new Flyway()
      fwy.setDataSource(url, user, password)
      fwy.setLocations(s"schema/$e")
      fwy.setBaselineOnMigrate(true)
      fwy.setSchemas(e)
      fwy.migrate()
    }
  }

  pg_schema.foreach { e =>
    test("[" + e.capitalize + "] schema test") {
      connect(e, { implicit c =>
        val expected = schema ~> e file "expected.json"
        val result = PGMetadataCollector().collect()

        compare(e,expected, result)
      })
    }
  }
}

trait Comparison extends Matchers with TestUtils {
  implicit val formats = DefaultFormats

  def compare(schema: String, file: File, seq: Seq[TableDTO]) {
    val expected = file.read
    val result = prettyRender(decompose(seq))

    Try {
      Comparator.MODE_STRICT.compare(expected, result)
    } match {
      case Success(_) => // ok
      case Failure(e) =>
        errors ~> schema file "expected.json" write expected
        errors ~> schema file "actual.json" write result
        throw e
    }
  }
}
