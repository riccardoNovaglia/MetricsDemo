package util

import akka.http.scaladsl.model.HttpResponse
import org.scalatest.{Assertion, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.implicitConversions

trait ServiceResponseVerification {

  implicit def httpResponseToServiceResponse(response: HttpResponse): ServiceResponse =
    new ServiceResponse(response)

  def isSuccessful(serviceResponse: ServiceResponse): Assertion =
    serviceResponse.isSuccessful
}

class ServiceResponse(httpResponse: HttpResponse) extends Matchers with SystemAndMaterializer {
  import actorSystem.dispatcher
  private val timeout: FiniteDuration = 200 millis

  def isSuccessful: Assertion = withClue(s"Was expecting successful response from service, but was \n$this") {
    httpResponse.status.intValue() should (equal(200) or equal(201))
  }

  override def toString: String =
    s"""
       |StatusCode: ${httpResponse.status.intValue()}
       |Body      : ${entityAsString(httpResponse)}
     """.stripMargin

  private def entityAsString(httpResponse: HttpResponse): String = {
    Await.result(httpResponse.entity.toStrict(timeout).map { _.data }.map(_.utf8String), 200 millis)
  }
}
