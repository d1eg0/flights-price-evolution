name := "flights-streams"

version := "0.1"

scalaVersion := "2.12.10"

val sparkVersion = "2.4.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion
)

val testDependencies = Seq(
  "org.scalatest" % "scalatest_2.12" % "3.1.1" % "test"
)

libraryDependencies ++= testDependencies
