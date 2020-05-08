package services

import org.apache.spark.sql.{Row, SparkSession}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class packageTest extends AnyFlatSpec with Matchers {

  val spark: SparkSession = SparkSession
    .builder()
    .master("local[1]")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  val fixture =
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
    val parsedDf = parse_json(fixture)
    val orderedDf = get_cheapest_flights(parsedDf, "5 minutes")
    val cheapPrices: Array[Row] = orderedDf.orderBy("amount").collect()
    val amounts: Array[Double] =
      cheapPrices.map(row => row.getAs[Double]("amount"))
    amounts.shouldBe(Array(15.99, 17.99))
  }
}