package test

import java.io.File
import org.apache.commons.io.FileUtils.{readFileToString,writeStringToFile}
import Ops._

trait TestUtils {
  lazy val userDir = System.getProperty("user.dir")
  lazy val test = new File(userDir) ~> "src" ~> "test"
  lazy val errors = test ~> "errors"
  lazy val resources = test ~> "resources"
  lazy val schema = resources ~> "schema"
}

object Ops {

  implicit class FileOperations(val file: File) extends AnyVal {
    def read: String = readFileToString(file)
    def write(content: String): File = {
      writeStringToFile(file, content)
      file
    }

    def ~>(path: String): File = new File(file, path)
    def files: Seq[File] = file.listFiles().toSeq
  }
}

