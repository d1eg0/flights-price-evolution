version := "1.0"
scalaVersion := "2.11.12"
organization := "com.d1eg0"

val sparkVersion = "2.4.5"

name := "FlightStreamsApp"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "com.github.scopt" %% "scopt" % "4.0.0-RC2",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.0.3",
  "org.scalatest" %% "scalatest" % "3.1.1" % Test
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _                                   => MergeStrategy.first
}

assemblyJarName in assembly := "flights-streams.jar"

test in assembly := {}
