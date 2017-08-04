package metricsDemoService.items

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.failed


class ItemsRepository(itemsClient: ItemsClient)
                     (implicit private val executionContext: ExecutionContext) {

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
