package services

import com.mongodb.client.model.Filters
import org.apache.spark.sql.ForeachWriter
import org.mongodb.scala._
import org.mongodb.scala.model.UpdateOptions
import org.mongodb.scala.model.Updates.{combine, set}

import scala.concurrent._
import scala.concurrent.duration.Duration

class MongoWriter(hosts: String) {
  private val uri: String =
    s"mongodb://appuser:apppass@$hosts/admin?retryWrites=true&w=majority"
  private val client: MongoClient = MongoClient(uri)
  private val collection: MongoCollection[Document] = client
    .getDatabase("flights")
    .getCollection("prices")

  /**
    * Updates the stored price if the new one is less than the stored price.
    * Creates a new price if there is not a stored price for the route and departure time of the new price.
    * If the new price is greater or equal is discarded.
    * @param value MinFlightPrice instance
    */
  def update(value: MinFlightPrice): Unit = {
    val findCondition = Filters.and(
      Filters.eq("ts", value.ts),
      Filters.eq("origin", value.origin),
      Filters.eq("destination", value.destination),
      Filters.eq("departureTime", value.departureTime)
    )
    val updateCondition = Filters.and(
      Filters.eq("ts", value.ts),
      Filters.eq("origin", value.origin),
      Filters.eq("destination", value.destination),
      Filters.eq("departureTime", value.departureTime),
      Filters.gt("amount", value.amount)
    )
    val newDoc = combine(
      set("ts", value.ts),
      set("origin", value.origin),
      set("destination", value.destination),
      set("departureTime", value.departureTime),
      set("flightNumber", value.flightNumber),
      set("amount", value.amount)
    )
    val onlyUpdatesDoc = combine(
      set("flightNumber", value.flightNumber),
      set("amount", value.amount)
    )
    val findFuture = collection.countDocuments(findCondition).toFuture()
    val countDocs: Long =
      Await.result(findFuture, Duration(2, "seconds"))
    val upsertDocument = countDocs == 0
    val updateOptions = UpdateOptions().upsert(upsertDocument)
    val future = collection
      .updateOne(
        updateCondition,
        if (upsertDocument) newDoc else onlyUpdatesDoc,
        updateOptions
      )
      .toFuture()
    Await.result(future, Duration(3, "seconds"))
  }

  def close(): Unit = {
    client.close()
  }
}

class MongoForeachWriter(hosts: String) extends ForeachWriter[MinFlightPrice] {
  @transient var writer: MongoWriter = _
  var localPartition: Long = 0
  var localEpochId: Long = 0

  override def open(partitionId: Long, epochId: Long): Boolean = {
    writer = new MongoWriter(hosts)
    localPartition = partitionId
    localEpochId = epochId
    true
  }

  override def process(value: MinFlightPrice): Unit = {
    writer.update(value)
  }

  override def close(errorOrNull: Throwable): Unit = {
    if (errorOrNull == null) {
      writer.close()
    } else {
      println("Error mongo writer:" + errorOrNull)
    }
  }
}
