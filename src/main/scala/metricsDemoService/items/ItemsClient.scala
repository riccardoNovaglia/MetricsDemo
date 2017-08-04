package metricsDemoService.items

import akka.actor.ActorSystem
import akka.stream.Materializer
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import metricsDemoService.http.SimpleHttpClient
import org.json4s.{jackson, DefaultFormats}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.{failed, successful}


class ItemsClient(httpClient: SimpleHttpClient)
                 (implicit private val actorSystem: ActorSystem, materializer: Materializer, executionContext: ExecutionContext)
  extends Json4sSupport {
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
