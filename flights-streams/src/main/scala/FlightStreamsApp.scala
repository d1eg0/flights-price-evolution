import org.apache.spark.sql.SparkSession
import services._
import config.ConfigHandler

object FlightStreamsApp extends App {

  val inputConfig = ConfigHandler.getInputConfig(args)
  ConfigHandler.print(inputConfig)

  implicit val spark: SparkSession = SparkSession.builder
    .appName("Flight Streams")
    .master(inputConfig.sparkMaster)
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  val flights = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "0.0.0.0:9092")
    .option("subscribe", "flights")
    .load()

  val flightsExploded = parse_json(flights)
  val cheapestFlights =
    get_cheapest_flights(flightsExploded, inputConfig.windowDuration)

  val query = cheapestFlights.writeStream
    .outputMode("complete")
    .format("console")
    .start()

  query.awaitTermination()

}
