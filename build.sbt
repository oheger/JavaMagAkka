/** Definition of versions. */
lazy val AkkaVersion = "2.5.0"
lazy val AkkaHttpVersion = "10.0.4"
lazy val VersionScala = "2.11.8"

lazy val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion,
  "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "org.scala-lang" % "scala-reflect" % VersionScala
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "junit" % "junit" % "4.12" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)

val defaultSettings = Seq(
  version := "1.0-SNAPSHOT",
  scalaVersion := VersionScala,
  libraryDependencies ++= akkaDependencies,
  libraryDependencies ++= testDependencies,
  libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
)

lazy val JavaMagAkka = (project in file("."))
  .settings(defaultSettings: _*)
  .settings(
    name := "javamag-akka-demo"
  ) 

