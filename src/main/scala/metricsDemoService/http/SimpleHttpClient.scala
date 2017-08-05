package metricsDemoService.http

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.client.RequestBuilding
import com.typesafe.config.Config
import metricsDemoService.util.Actors

import scala.concurrent.Future


class SimpleHttpClient(httpClientConfig: HttpClientConfig, akkaHttp: HttpExt)(implicit private val actors: Actors) {

  import actors._

  def get(url: String): Future[ConsumedResponse] = {
    akkaHttp.singleRequest(RequestBuilding.Get(url))
      .map(res => ConsumedResponse(res))
  }
}
class HttpTimeoutException()

class HttpClientConfig(config: Config)
