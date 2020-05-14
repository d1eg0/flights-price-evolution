package object services {

  import org.mongodb.scala.bson._
  import entities.InputSchema
  import org.apache.spark.sql.functions.{
    col,
    element_at,
    explode,
    from_json,
    min,
    unix_timestamp,
    window
  }
  import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
  import org.apache.spark.sql.types.TimestampType

  case class FlightPrice(
      ts: java.sql.Timestamp,
      origin: String,
      destination: String,
      flightNumber: String,
      departureTime: java.sql.Timestamp,
      arrivalTime: java.sql.Timestamp,
      amount: Double
  )

  case class MinFlightPrice(
      ts: java.sql.Timestamp,
      origin: String,
      destination: String,
      departureTime: java.sql.Timestamp,
      amount: Double
  ) {
    def toJson: Document = {
      Document(
        "ts" -> this.ts,
        "origin" -> this.origin,
        "destination" -> this.destination,
        "departureTime" -> this.departureTime,
        "amount" -> this.amount
      )
    }
  }

  def parse_json(
      df: DataFrame
  )(implicit sparkSession: SparkSession): Dataset[FlightPrice] = {
    import sparkSession.implicits._
    df.select(
        from_json(col("value").cast("string"), InputSchema.schema).alias("data")
      )
      .select("data.*")
      .select(col("ts"), explode(col("trips")).alias("trips"))
      .select(col("ts"), col("trips.*"))
      .select(
        col("ts"),
        col("origin"),
        col("destination"),
        explode(col("dates")).alias("dates")
      )
      .select(col("ts"), col("origin"), col("destination"), col("dates.*"))
      .select(
        col("ts"),
        col("origin"),
        col("destination"),
        explode(col("flights")).alias("flights")
      )
      .select(col("ts"), col("origin"), col("destination"), col("flights.*"))
      .select(
        col("ts"),
        col("origin"),
        col("destination"),
        col("flight_number"),
        element_at(col("time"), 1).alias("departure_time"),
        element_at(col("time"), 2).alias("arrival_time"),
        explode(col("fares")).alias("fare")
      )
      .select(
        unix_timestamp(col("ts"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          .cast(TimestampType)
          .alias("ts"),
        col("origin"),
        col("destination"),
        col("flight_number").as("flightNumber"),
        unix_timestamp(col("departure_time"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          .cast(TimestampType)
          .alias("departureTime"),
        unix_timestamp(col("arrival_time"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          .cast(TimestampType)
          .alias("arrivalTime"),
        col("fare.amount").alias("amount")
      )
      .as[FlightPrice]

  }

  def get_cheapest_flights(
      df: Dataset[FlightPrice],
      windowDuration: String
  )(implicit sparkSession: SparkSession): Dataset[MinFlightPrice] = {
    import sparkSession.implicits._
    df.withWatermark("ts", "30 seconds")
      .groupBy(
        window(col("ts"), windowDuration),
        col("origin"),
        col("destination"),
        col("departureTime")
      )
      .agg(min(col("amount")).alias("amount"))
      .select(
        col("window.start").as("ts"),
        col("origin"),
        col("destination"),
        col("departureTime"),
        col("amount")
      )
      .as[MinFlightPrice]
  }

}
