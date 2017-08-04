package metricsDemoService.items

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import metricsDemoService.http.SimpleHttpClient
import metricsDemoService.util.Actors
import org.json4s.{jackson, DefaultFormats}

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}


class ItemsClient(httpClient: SimpleHttpClient)(implicit private val actors: Actors) extends Json4sSupport {
  import actors._

  private implicit val formats = DefaultFormats
  private implicit val serialization = jackson.Serialization

  def getAllItems: Future[List[Item]] =
    httpClient.get("getItems")
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
