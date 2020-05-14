package config

import scopt.OParser

case class Config(
    windowDuration: String = "1 minutes",
    sparkMaster: String = "local[2]"
)

object InputConfigParser {
  def getInputConfig(args: Array[String]): Config = {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName("scopt"),
        head("scopt", "4.x"),
        opt[String]('w', "window-duration")
          .action((x, c) => c.copy(windowDuration = x))
          .text("window duration. E.g. '10 minutes'"),
        opt[String]('m', "master")
          .action((x, c) => c.copy(sparkMaster = x))
          .text("Spark master host. E.g. spark://localhost:7077")
      )
    }

    var inputConfig: Config = null
    OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        inputConfig = config.copy()
      case _ =>
        sys.exit(-1)
    }
    inputConfig
  }

  def print(config: Config) = {
    println(s"Spark master: ${config.sparkMaster}")
    println(s"Window duration: ${config.windowDuration}")
  }

}
