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

class Service(config: ServiceConfiguration)
             (implicit val actorSystem: ActorSystem, materializer: Materializer) {
  def getItems: Future[HttpResponse] =
    get(Endpoints.getItems)

  def sayHello: Future[HttpResponse] =
    get(Endpoints.sayHello)

  private def get(endpoint: String): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = endpoint))

  object Endpoints {
    val getItems: String = config.baseUrl + "items"
    val sayHello: String = config.baseUrl + "hello"
  }
}

class ServiceConfiguration(config: Config) {
  val baseUrl: String = config.getString("baseUrl") + "/"
}
