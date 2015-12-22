name := "pg_metadata"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe.play" %% "anorm" % "3.0.0-SNAPSHOT",
  "org.flywaydb" % "flyway-core" % "3.2.1",
  "net.liftweb" %% "lift-json" % "3.0-M7",
  "imagination" % "comparator_2.11" % "0.4-SNAPSHOT",
  "com.typesafe" % "config" % "1.3.0"
)

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.4").map(_ % Test)

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("public"))


parallelExecution in Test := false