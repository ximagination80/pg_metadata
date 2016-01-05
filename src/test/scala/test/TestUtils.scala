package test

import java.io.File

import org.apache.commons.io.FileUtils.{forceDelete, readFileToString, writeStringToFile}

trait TestUtils {
  lazy val `user.dir` = new File(System.getProperty("user.dir"))
  lazy val test = `user.dir` ~> "src" ~> "test"
  lazy val errors = test ~> "errors"
  lazy val resources = test ~> "resources"
  lazy val schema = resources ~> "schema"

  implicit class FileOperations(file: File)  {
    def read: String = readFileToString(file)
    def write(content: String): File = {
      writeStringToFile(file, content)
      file
    }
    def remove(): Unit = forceDelete(file)
    def ~>(path: String): File = new File(file, path)
    def files: Seq[File] = file.listFiles().toSeq
  }
}
