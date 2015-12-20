name := "pg_metadata"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe.play" %% "anorm" % "3.0.0-SNAPSHOT"
)

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("public"))
