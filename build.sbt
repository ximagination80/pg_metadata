organization := "org.imagination"

name := "pg_metadata"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "com.typesafe.play" % "anorm_2.11" % "2.3.10"
)

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.flywaydb" % "flyway-core" % "3.2.1" % "test",
  "net.liftweb" %% "lift-json" % "3.0-M7" % "test",
  "org.imagination" % "comparator_2.11" % "1.1" % "test",
  "commons-io" % "commons-io" % "2.4" % "test")

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("public"))

scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "UTF-8")

crossScalaVersions := Seq("2.11.7","2.10.5")

description := """a postgres database metadata collecting tool """

licenses := Seq("The Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/ximagination80/pg_metadata"))

coverageEnabled.in(Test, test) := true

coverageMinimum := 95

coverageFailOnMinimum := true

parallelExecution in Test := false

publishMavenStyle := true

publishTo :=
  Some(Resolver.file("file", new File(Path.userHome.absolutePath+"/.m2/repository")))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
