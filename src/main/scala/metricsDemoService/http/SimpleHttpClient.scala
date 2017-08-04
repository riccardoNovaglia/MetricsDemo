package metricsDemoService.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}


class SimpleHttpClient()
                      (implicit private val actorSystem: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) {
  def get(uri: String): Future[ConsumedResponse] =
    Http().singleRequest(RequestBuilding.Get(s"http://localhost:19999/$uri"))
    .map(res => ConsumedResponse(res))
}
