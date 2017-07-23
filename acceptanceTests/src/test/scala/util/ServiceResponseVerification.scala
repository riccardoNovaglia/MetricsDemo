package util

import akka.http.scaladsl.model.HttpResponse
import org.scalatest.{Assertion, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.implicitConversions

trait ServiceResponseVerification {
  implicit def httpResponseToServiceResponse(response: HttpResponse): ServiceResponse =
    new ServiceResponse(response)
}

class ServiceResponse(httpResponse: HttpResponse) extends Matchers with SystemAndMaterializer {
  import actorSystem.dispatcher
  private val timeout: FiniteDuration = 200 millis

  val statusCode: Int     = httpResponse.status.intValue()
  val body      : String  = entityAsString(httpResponse)

  def isSuccessful(): Assertion = withClue(s"Was expecting successful response from service, but was \n$this") {
     statusCode should (equal(200) or equal(201))
  }

  def contains(aString: String): Assertion = {
    body should include(aString)
  }

  override def toString: String =
    s"""
       |StatusCode: $statusCode
       |Body      : $body
     """.stripMargin

  private def entityAsString(httpResponse: HttpResponse): String = {
    Await.result(httpResponse.entity.toStrict(timeout).map { _.data }.map(_.utf8String), 200 millis)
  }
}
