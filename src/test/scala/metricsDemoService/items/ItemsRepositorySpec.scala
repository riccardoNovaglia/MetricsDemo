package metricsDemoService.items

import metricsDemoService.TestActorSystemAndMaterializer
import org.json4s.DefaultFormats
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future.{failed, successful}

class ItemsRepositorySpec
  extends FreeSpec
          with Matchers
          with MockFactory
          with ScalaFutures
          with TestActorSystemAndMaterializer
{
  implicit val formats = DefaultFormats
  val itemsClient: ItemsRetriever = stub[ItemsRetriever]

  val itemsRepo = new ItemsRepository(itemsClient)
  val itemsList = List(Item("name", 123))
  val failureCause = new RuntimeException("Something happened")

  "The Items Repository" - {
    "should call dependency A to get the list of items" in {
      itemsClient.getAllItems _ when() returns successful(itemsList)

      whenReady(itemsRepo.getItems) { result =>
        result should be(itemsList)
      }
    }


    "should retry once if the dependency fails" in {
      itemsClient.getAllItems _ when() returns failed(failureCause) once()
      itemsClient.getAllItems _ when() returns successful(itemsList)

      whenReady(itemsRepo.getItems) { result =>
        result should be(itemsList)
        itemsClient.getAllItems _ verify() twice()
      }
    }

    "should give up and throw an exception if the dependency fails twice" in {
      itemsClient.getAllItems _ when() returns failed(failureCause)

      whenReady(itemsRepo.getItems.failed) { ex =>
        ex shouldBe a[ItemsRetrievalFailureException]
        ex.getMessage should be(s"Getting items from client failed twice with failures: ${Seq(failureCause, failureCause)}")
      }
    }
  }
}
