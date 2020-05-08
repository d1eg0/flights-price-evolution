import org.apache.spark.sql.SparkSession
import services._

object FlightStreamsApp extends App {

  val spark = SparkSession.builder
    .appName("Flight Streams")
    .master("local[2]")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  val flights = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "0.0.0.0:9092")
    .option("subscribe", "flights")
    .load()

  val flightsExploded = parse_json(flights)
  val cheapestFlights = get_cheapest_flights(flightsExploded, "10 minutes")

  val query = cheapestFlights.writeStream
    .outputMode("complete")
    .format("console")
    .start()

  query.awaitTermination()

}
