package entities

import org.apache.spark.sql.types.{
  ArrayType,
  DoubleType,
  StringType,
  StructField,
  StructType
}

object InputSchema {
  val faresSchema =
    ArrayType(StructType(Seq(StructField("amount", DoubleType, false))), false)

  val flightsSchema = ArrayType(
    StructType(
      Seq(
        StructField("fares", faresSchema, false),
        StructField("flight_number", StringType, false),
        StructField("time", ArrayType(StringType), false)
      )
    ),
    false
  )

  val datesSchema = ArrayType(
    StructType(
      Seq(
        StructField("date_out", StringType, false),
        StructField("flights", flightsSchema, false)
      )
    ),
    false
  )

  val tripsSchema = ArrayType(
    StructType(
      Seq(
        StructField("origin", StringType, false),
        StructField("destination", StringType, false),
        StructField("dates", datesSchema, false)
      )
    ),
    false
  )

  val schema = StructType(
    Seq(
      StructField("ts", StringType, false),
      StructField("trips", tripsSchema, false)
    )
  )

}
