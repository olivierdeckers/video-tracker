name := """video-tracker"""
version := "0.1.0-SNAPSHOT"

description := ""

scalaVersion := "2.11.7"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "0.5.1",
  "org.sangria-graphql" %% "sangria-spray-json" % "0.1.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.0"
)

Revolver.settings
enablePlugins(JavaAppPackaging)

fork in run := true