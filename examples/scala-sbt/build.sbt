ThisBuild / scalaVersion := "3.3.7"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "io.github.frikit.krandom.examples"

lazy val krandomVersion = sys.props.getOrElse("krandom.version", sys.env.getOrElse("KRANDOM_VERSION", "0.1.0-SNAPSHOT"))

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "io.github.frikit" % "krandom-core" % krandomVersion,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)
