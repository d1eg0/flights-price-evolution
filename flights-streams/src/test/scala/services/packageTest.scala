package services

import java.sql.Timestamp
import java.text.SimpleDateFormat

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class packageTest extends AnyFlatSpec with Matchers {

  private implicit val spark: SparkSession = SparkSession
    .builder()
    .master("local[1]")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  private val fixture: DataFrame =
    spark.read.text("src/test/resources/fixtures/ryanair_data.json")

  "parse_json" should "parse the JSON Kafka message correctly" in {
    val parsedDf = parse_json(fixture)
    parsedDf.count() shouldBe 6
    parsedDf
      .select("origin")
      .dropDuplicates()
      .orderBy("origin")
      .collect()
      .map(_.getAs[String]("origin")) shouldBe Array("BCN", "PMI")
  }

  "get_cheapest_flights" should "filter cheapest prices by route" in {
    import spark.implicits._
    val now = new Timestamp(System.currentTimeMillis())
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    val departureDate =
      new Timestamp(dateFormat.parse("2020-07-01 10:00:00.000").getTime)
    val arrivalDate =
      new Timestamp(dateFormat.parse("2020-07-01 11:00:00.000").getTime)
    val flightsFixture = Seq(
      FlightPrice(
        ts = now,
        origin = "PMI",
        destination = "MAD",
        flightNumber = "VY 3435",
        departureTime = departureDate,
        arrivalTime = arrivalDate,
        amount = 15.99
      ),
      FlightPrice(
        ts = now,
        origin = "PMI",
        destination = "MAD",
        flightNumber = "VY 3435",
        departureTime = departureDate,
        arrivalTime = arrivalDate,
        amount = 19.75
      ),
      FlightPrice(
        ts = now,
        origin = "MAD",
        destination = "PMI",
        flightNumber = "VY 3438",
        departureTime = departureDate,
        arrivalTime = arrivalDate,
        amount = 17.99
      ),
      FlightPrice(
        ts = now,
        origin = "MAD",
        destination = "PMI",
        flightNumber = "VY 3438",
        departureTime = departureDate,
        arrivalTime = arrivalDate,
        amount = 19.75
      )
    ).toDS()

    val orderedDf = get_cheapest_flights(flightsFixture, "5 minutes")
    val cheapPrices: Array[MinFlightPrice] =
      orderedDf.orderBy("amount").collect()
    val amounts: Array[Double] =
      cheapPrices.map(row => row.amount)
    amounts.shouldBe(Array(15.99, 17.99))
  }
}
