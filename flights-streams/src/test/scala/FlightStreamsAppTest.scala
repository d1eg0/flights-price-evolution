import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import services.{get_cheapest_flights, parse_json}

class FlightStreamsAppTest extends AnyFlatSpec {

  val spark: SparkSession = SparkSession
    .builder()
    .master("local[1]")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  "parse json" should "parse flights correctly" in {
    val fixture =
      spark.read.text("src/test/resources/fixtures/ryanair_data.json")
    fixture.show()
    val parsedDf = parse_json(fixture)
    parsedDf.show(false)
    parsedDf.printSchema()
  }

  "get_cheapest_flights" should "order flights by cheapes price" in {
    val fixture =
      spark.read.text("src/test/resources/fixtures/ryanair_data.json")
    fixture.show()
    val parsedDf = parse_json(fixture)
    val orderedDf = get_cheapest_flights(parsedDf)
    orderedDf.show(false)
  }
}
