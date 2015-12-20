package test

import java.io.File
import org.apache.commons.io.FileUtils

trait TestUtils {
  require(resources.exists(), "Unable to find path to test data directory")

  def userDir = System.getProperty("user.dir")
  def resources = new File(userDir, "/src/test/resources")
  def schema = resources ~> "schema"
  def errors = new File(userDir, "/src/test/errors")
  def list(): Seq[File] = resources.listFiles().toSeq

  implicit class FileOperations(file: File) {
    def write(content: String): File = {
      FileUtils.writeStringToFile(file, content)
      file
    }
    def read: String = FileUtils.readFileToString(file)
    def all():Seq[File]= file.listFiles().toSeq

    def ~>(path:String): File = new File(file,path)
    def <~(): File = file.getParentFile
    def file(name: String): File = new File(file, name)
  }
}
