name := """video-tracker"""
version := "0.1.0-SNAPSHOT"

description := ""

scalaVersion := "2.11.8"
scalacOptions ++= Seq("-deprecation", "-feature")

val akkaVersion = "2.4.8"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "0.5.1",
  "org.sangria-graphql" %% "sangria-spray-json" % "0.1.0",
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-core" % "2.3.0",
  "com.sksamuel.elastic4s" %% "elastic4s-streams" % "1.7.4",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.7.2"
)

Revolver.settings
Revolver.enableDebugging(port = 5050, suspend = false)

fork in run := true