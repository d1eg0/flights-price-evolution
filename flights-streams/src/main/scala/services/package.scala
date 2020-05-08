package object services {

  import entities.InputSchema
  import org.apache.spark.sql.DataFrame
  import org.apache.spark.sql.functions.{
    col,
    element_at,
    explode,
    from_json,
    min,
    unix_timestamp,
    window
  }

  def parse_json(df: DataFrame): DataFrame = {
    import org.apache.spark.sql.types.{DateType, TimestampType}
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
        col("flight_number"),
        unix_timestamp(col("departure_time"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          .cast(TimestampType)
          .cast(DateType)
          .alias("departure_time"),
        unix_timestamp(col("arrival_time"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          .cast(TimestampType)
          .cast(DateType)
          .alias("arrival_time"),
        col("fare.amount").alias("amount")
      )

  }

  def get_cheapest_flights(df: DataFrame, windowDuration: String): DataFrame = {
    df.groupBy(
        window(col("ts"), windowDuration, "5 minutes"),
        col("origin"),
        col("destination"),
        col("departure_time")
      )
      .agg(min(col("amount")).alias("amount"))
  }

}
