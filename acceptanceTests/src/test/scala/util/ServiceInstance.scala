package util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.typesafe.config.Config
import org.scalatest.Assertion

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.implicitConversions

trait ServiceInstance extends TestsConfiguration with SystemAndMaterializer {
  implicit val service: Service = new Service(serviceConfiguration)
}

class Service(config: ServiceConfiguration)
             (implicit val actorSystem: ActorSystem, val materializer: Materializer)
  extends CallsSugar {
  def getItems: Future[HttpResponse] =
    get(Endpoints.getItems)

  def getItems(executionSteps: => Unit): Thenable = {
    executionSteps
    val response: HttpResponse = Await.result(get(Endpoints.getItems), 10 seconds)
    new Thenable(new ServiceResponse(response))
  }

  def getItems1: Dependable = {
    println("Getting items from the service,\n")
    new Dependable(Endpoints.getItems)
  }

  def sayHello: Future[HttpResponse] =
    get(Endpoints.sayHello)

  object Endpoints {
    val getItems: String = config.baseUrl + "items"
    val sayHello: String = config.baseUrl + "hello"
  }
}

trait CallsSugar {
  implicit val actorSystem: ActorSystem
  implicit val materializer: Materializer

  def get(endpoint: String): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = endpoint))
}

class ServiceConfiguration(config: Config) {
  val baseUrl: String = config.getString("baseUrl") + "/"
}

class Dependable(url: String)(implicit val actorSystem: ActorSystem, val materializer: Materializer) extends CallsSugar {
  def Given(executionSteps: => Unit)(implicit service: Service): Thenable = {
    executionSteps
    val response: HttpResponse = Await.result(get(url), 10 seconds)
    new Thenable(new ServiceResponse(response))
  }
}

class Thenable(response: ServiceResponse) {
  def Then(verifications: ServiceResponse => Unit): Unit = {
    verifications(response)
  }
}

trait Magic {
  def isSuccessful(implicit result: ServiceResponse): Assertion = {
    println("Will return successfully (200)")
    result.isSuccessful()
  }
  def contains(i: Int, str: String)(implicit result: ServiceResponse): Assertion = {
    println(s"And contain $i instances of $str in ${result.body}")
    result.contains(i, str)
  }
}
