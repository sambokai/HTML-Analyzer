name := "S24_TechChallenge"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.4"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.196"


libraryDependencies += "org.mockito" % "mockito-core" % "2.13.0" % "test"

// Play WS - HTTP Client
libraryDependencies += ws

// https://mvnrepository.com/artifact/org.jsoup/jsoup
libraryDependencies += "org.jsoup" % "jsoup" % "1.10.3"

// WebJars
libraryDependencies ++= Seq(
  "org.webjars" % "bootstrap" % "4.0.0-beta.2",
  "org.webjars" % "jquery" % "3.2.1",
  "org.webjars.npm" % "popper.js" % "1.13.0"
)