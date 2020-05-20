package config

import scopt.OParser

case class Config(
    windowDuration: String = "1 minutes",
    kafkaBootstrapServer: String = "0.0.0.0:9092",
    kafkaTopic: String = "flights",
    mongoHosts: String = "0.0.0.0:27017"
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
        opt[String]('b', "bootstrap-server")
          .action((x, c) => c.copy(kafkaBootstrapServer = x))
          .text("Kafka bootstrap server. E.g. 0.0.0.0:9092"),
        opt[String]('m', "mongo-hosts")
          .action((x, c) => c.copy(mongoHosts = x))
          .text("MongoDB hosts. E.g. 0.0.0.0:27017"),
        opt[String]('t', "topic")
          .action((x, c) => c.copy(kafkaTopic = x))
          .text("Kafka topic. E.g. flights")
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

  def print(config: Config): Unit = {
    println(s"Window duration: ${config.windowDuration}")
    println(s"Kafka bootstrap server: ${config.kafkaBootstrapServer}")
    println(s"Kafka topic: ${config.kafkaTopic}")
    println(s"Mongo hosts: ${config.mongoHosts}")
  }

}
