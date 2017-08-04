package metricsDemoService.http

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import metricsDemoService.util.Actors

import scala.concurrent.Future


class SimpleHttpClient()(implicit private val actors: Actors) {
  import actors._

  def get(uri: String): Future[ConsumedResponse] =
    Http().singleRequest(RequestBuilding.Get(s"http://localhost:19999/$uri"))
      .map(res => ConsumedResponse(res))
}
