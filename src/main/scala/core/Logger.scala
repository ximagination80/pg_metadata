package core

trait Logger {
  def log(msg: String): Unit
}

object EmptyLogger extends Logger {
  def log(msg: String) = {}
}
