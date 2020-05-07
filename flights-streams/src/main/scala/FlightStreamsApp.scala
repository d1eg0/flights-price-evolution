import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, min, window}
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
  val cheapestFlights = flightsExploded
    .groupBy(
      window(col("ts"), "10 minutes", "5 minutes"),
      col("origin"),
      col("destination"),
      col("departure_time")
    )
    .agg(min(col("amount")))
  //val cheapestFlights = get_cheapest_flights(group)

  val query = cheapestFlights.writeStream
    .outputMode("complete")
    .format("console")
    .start()

  query.awaitTermination()

}
