package core

trait Logger {
  def debug:Boolean
  def log(msg: String): Unit
  def logF(template: String, args: String*): Unit
}
