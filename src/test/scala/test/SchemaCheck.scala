package test

import comparator._
import core.{AppSettings, PGMetadataCollector}
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST._
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class SchemaCheck extends FunSuite
      with PGConnectionUtils
      with BeforeAndAfterAll
      with Comparison {

  implicit val settings = AppSettings(debug = false)
  implicit val formats = DefaultFormats

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
        val expected = (schema ~> e file "expected.json").read
        val actual = PGMetadataCollector().collect()

        compare(e, expected, prettyRender(decompose(actual)))
      })
    }
  }
}

trait Comparison extends TestUtils {

  def compare(schema: String, expected: String, actual: String) {
    try Comparator.MODE_STRICT.compare(expected, actual) catch {
      case e: Exception =>
        errors ~> schema file "expected.json" write expected
        errors ~> schema file "actual.json" write actual
        throw e
    }
  }
}
