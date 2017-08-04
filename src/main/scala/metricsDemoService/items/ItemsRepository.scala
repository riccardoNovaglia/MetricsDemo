package metricsDemoService.items

import metricsDemoService.util.Actors

import scala.concurrent.Future
import scala.concurrent.Future.failed


class ItemsRepository(itemsClient: ItemsClient)(implicit private val actors: Actors) {
  import actors._

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
  extends Exception(s"Getting items from client failed twice with failures: $exceptions", exceptions.last)
