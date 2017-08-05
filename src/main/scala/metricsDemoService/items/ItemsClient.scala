package metricsDemoService.items

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import metricsDemoService.http.SimpleHttpClient
import metricsDemoService.util.Actors
import org.json4s.{jackson, DefaultFormats}

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

trait ItemsRetriever {
  def getAllItems: Future[List[Item]]
}


class ItemsClient(httpClient: SimpleHttpClient)(implicit private val actors: Actors) extends Json4sSupport with ItemsRetriever {
  import actors._

  private implicit val formats = DefaultFormats
  private implicit val serialization = jackson.Serialization

  val baseUrl = "http://localhost:19999"

  def getAllItems: Future[List[Item]] =
    httpClient.get(s"$baseUrl/getItems")
      .flatMap(res => res.jsonBodyAs[List[Item]].flatMap {
        case Nil => failed(NoItemsReturnedException())
        case items => successful(items)
      })
      .recoverWith {
        case noItems: NoItemsReturnedException => failed(noItems)
        case e: Throwable => failed(ItemsClientFailureException(e)) // TODO: count unmarshalling failures?
      }
}

case class ItemsClientFailureException(e: Throwable)
  extends RuntimeException(s"Failed to get items caused by http client failure: ${e.getMessage}.", e)

case class NoItemsReturnedException()
  extends RuntimeException("No items were returned by the items service.")
