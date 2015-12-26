package test

import comparator._
import console.AppSettings
import core.PGMetadataCollector
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST._
import org.flywaydb.core.Flyway
import org.scalatest.FunSuite

class SchemaCheck extends FunSuite
      with PGConnectionUtils
      with TestUtils {

  implicit val settings = AppSettings(debug = false)
  implicit val formats = DefaultFormats

  val availableSchemas = schema.files.map(_.getName).sorted

  availableSchemas.foreach { e =>
    test("[" + e.capitalize + "] MIGRATION ") {
      val fwy = new Flyway()
      fwy.setDataSource(url, user, password)
      fwy.setLocations(s"schema/$e")
      fwy.setBaselineOnMigrate(true)
      fwy.setSchemas(e)
      fwy.migrate()
    }
  }

  availableSchemas.foreach { e =>
    test("[" + e.capitalize + "] TEST ") {
      connect(e, { implicit connection =>
        val expected = (schema ~> e ~> "expected.json").read
        val actual = PGMetadataCollector(e).collect()

        compare(e, expected, prettyRender(decompose(actual)))
      })
    }
  }

  def compare(schema: String, expected: String, actual: String) {
    try Comparator.MODE_STRICT.compare(expected, actual) catch {
      case e: Exception =>
        errors ~> schema ~> "expected.json" write expected
        errors ~> schema ~> "actual.json" write actual
        throw e
    }
  }
}
