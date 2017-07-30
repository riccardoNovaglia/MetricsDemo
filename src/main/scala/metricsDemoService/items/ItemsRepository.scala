package metricsDemoService.items

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.Future
import scala.concurrent.Future.failed


class ItemsRepository(itemsClient: ItemsClient)(implicit val actorSystem: ActorSystem, materializer: Materializer) {
  import actorSystem.dispatcher

  def getItems: Future[List[Item]] =
    itemsClient.getAllItems
      .recoverWith {
        case ex: Throwable => tryAgain(ex)
      }

  private def tryAgain(previousException: Throwable): Future[List[Item]] =
    itemsClient.getAllItems
      .recoverWith {
        case ex: Throwable => failed(ItemsRetrievalFailureException(Seq(previousException, ex)))
      }
}

case class ItemsRetrievalFailureException(exceptions: Seq[Throwable])
  extends Exception(s"Getting items from client failed twice with failures: $exceptions")
