import org.apache.spark.sql.SparkSession
import services._
import config.InputConfigParser

object FlightStreamsApp extends App {

  val inputConfig = InputConfigParser.getInputConfig(args)
  InputConfigParser.print(inputConfig)

  implicit val spark: SparkSession = SparkSession.builder
    .appName("Flight Streams")
    .master(inputConfig.sparkMaster)
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

  val writerInstance = new MongoForeachWriter

  val query = cheapestFlights.writeStream
    .foreach(writerInstance)
    .outputMode("append")
    .start()

  query.awaitTermination()

}
