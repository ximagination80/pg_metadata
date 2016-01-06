package test

import comparator._
import core.PGMetadataCollector
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST._
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class SchemaCheck extends FunSuite
      with PGConnectionUtils
      with TestUtils with BeforeAndAfterAll {

  implicit val formats = DefaultFormats

  val availableSchemas = schema.files.map(_.getName).sorted

  override protected def beforeAll() ={
    errors.mkdirs()
    errors.files.foreach(_.remove())
  }

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
      connect(e, { implicit c =>
        val expected = (schema ~> e ~> "expected.json").read
        val actual = PGMetadataCollector(e).collect()

        compare(e, expected, prettyRender(decompose(actual)))
      })
    }
  }

  def compare(schema: String, expected: String, actual: String) {
    try Comparator.strict.compare(expected, actual) catch {
      case e: Exception =>
        errors ~> schema ~> "expected.json" write expected
        errors ~> schema ~> "actual.json" write actual
        throw e
    }
  }
}
