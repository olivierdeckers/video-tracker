name := """video-tracker"""
version := "0.1.0-SNAPSHOT"

description := ""

scalaVersion := "2.11.7"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "0.5.1",
  "org.sangria-graphql" %% "sangria-spray-json" % "0.1.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.8",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.8",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.8",
  "com.typesafe.akka" %% "akka-cluster" % "2.4.8",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.8",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

Revolver.settings
//Revolver.enableDebugging(port = 5050, suspend = false)

fork in run := true