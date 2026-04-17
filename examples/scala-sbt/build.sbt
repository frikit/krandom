ThisBuild / scalaVersion := "3.3.7"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "org.github.krandom.examples"

resolvers += "GitHub Packages" at "https://maven.pkg.github.com/frikit/krandom"

libraryDependencies ++= Seq(
  "io.github.frikit" % "krandom-core" % "0.1.0",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)
