package metricsDemoService

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import metricsDemoService.WebServer.Item
import org.json4s.{jackson, DefaultFormats}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.Future.successful

class ItemsRoutingSpec
  extends FreeSpec
          with Matchers
          with MockFactory
          with ScalatestRouteTest
          with Json4sSupport {

  val itemsRepository: ItemsRepository = stub[ItemsRepository]
  val routingService: ItemsRouting = new ItemsRouting(itemsRepository)

  implicit val formats = DefaultFormats
  implicit val serialization = jackson.Serialization

  "The Routing Service" - {
    "Items retrieval" - {
      "should return items returned by the repository" in {
        itemsRepository.getItems _ when () returns successful(List()) once()
        Get("/items") ~> routingService.getItems ~> check {
          responseAs[List[Item]] should be(List())
        }

        val items = List(Item("anItem", 132L), Item("anotherItem", 321L))
        itemsRepository.getItems _ when () returns successful(items)
        Get("/items") ~> routingService.getItems ~> check {
          responseAs[List[Item]] should be(items)
        }
      }
    }
  }
}
