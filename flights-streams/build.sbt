version := "1.0"
scalaVersion := "2.12.10"
organization := "com.d1eg0"

val sparkVersion = "2.4.4"

lazy val config = (project in file("."))
  .settings(
    name := "flights-streams",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
      "com.github.scopt" %% "scopt" % "4.0.0-RC2",
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.0.3",
      "org.scalatest" %% "scalatest" % "3.1.1" % Test
    )
  )

// Docker conf
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerExposedPorts ++= Seq(4040)
