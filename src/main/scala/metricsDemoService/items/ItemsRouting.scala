package metricsDemoService.items

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.{complete, get, path}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{jackson, DefaultFormats}

class ItemsRouting(itemsRepository: ItemsRepository)
  extends Json4sSupport {
  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats

  def getItems: Route = {
    get {
      path("items") {
        complete(itemsRepository.getItems)
      }
    }
  }
}
