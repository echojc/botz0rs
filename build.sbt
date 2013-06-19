name := "botz0rs"

version := "0.0.1"

scalaVersion := "2.10.2"

organization := "sh.echo"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.scalaz" %% "scalaz-core" % "7.0.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.0-RC1"
)
