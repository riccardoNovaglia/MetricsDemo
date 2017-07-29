package util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.Future
import scala.language.implicitConversions

trait ServiceInstance extends TestsConfiguration with SystemAndMaterializer {
  val service: Service = new Service(serviceConfiguration)
}

class Service(serviceConfiguration: ServiceConfiguration)
             (implicit val actorSystem: ActorSystem, materializer: Materializer) {
  def getItems: Future[HttpResponse] =
    get(serviceConfiguration.Endpoints.getItems)

  def sayHello: Future[HttpResponse] =
    get(serviceConfiguration.Endpoints.sayHello)

  private def get(endpoint: String): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = endpoint))
}

class ServiceConfiguration(config: Config) {
  val baseUrl: String = config.getString("baseUrl") + "/"

  object Endpoints {
    val getItems: String = baseUrl + "items"
    val sayHello: String = baseUrl + "hello"
  }
}
