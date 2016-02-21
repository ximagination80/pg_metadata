package com.imagination.pg_metadata

trait Logger {
  def log(msg: String): Unit
}

object EmptyLogger extends Logger {
  def log(msg: String) = {}
}
