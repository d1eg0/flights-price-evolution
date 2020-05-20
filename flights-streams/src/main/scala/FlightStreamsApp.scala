import org.apache.spark.sql.SparkSession
import config.InputConfigParser
import services.{MongoForeachWriter, _}
import org.apache.spark.SparkConf

object FlightStreamsApp {

  def main(args: Array[String]): Unit = {

    val inputConfig = InputConfigParser.getInputConfig(args)
    InputConfigParser.print(inputConfig)
    val settings = Map[String, String](
      "spark.sql.session.timeZone" -> "UTC",
      "spark.driver.memory" -> "512m",
      "spark.executor.memory" -> "512m",
      "spark.cores.max" -> "2",
      "spark.driver.supervise" -> "true",
      "spark.default.parallelism" -> "10",
      "spark.sql.shuffle.partitions" -> "10"
    )

    val sparkConf: SparkConf =
      scala.util.Properties.envOrElse("MASTER", null) match {
        case null =>
          new SparkConf().setAll(settings)
        case master =>
          new SparkConf().setAll(settings).setMaster(master)
      }

    implicit val spark: SparkSession = SparkSession.builder
      .appName("Flight Streams")
      .config(sparkConf)
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val flights = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", inputConfig.kafkaBootstrapServer)
      .option("startingoffsets", "earliest")
      .option("subscribe", inputConfig.kafkaTopic)
      .load()

    val flightsExploded = parse_json(flights)
    val cheapestFlights =
      get_cheapest_flights(flightsExploded, inputConfig.windowDuration)

    val writerInstance = new MongoForeachWriter(inputConfig.mongoHosts)

    val query = cheapestFlights.writeStream
      .foreach(writerInstance)
      .outputMode("append")
      .start()

    query.awaitTermination()
  }
}
