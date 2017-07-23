import akka.http.scaladsl.model.HttpResponse
import org.json4s.{DefaultFormats, JValue}
import org.json4s.Extraction.decompose
import org.json4s.native.JsonMethods

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}


trait JsonSupport { this: WithActorSystemAndMaterializer =>
  implicit val formats = DefaultFormats

  private val timeout: FiniteDuration = 2000 millis

  implicit def stringToJson(string: String): JValue =
    JsonMethods.parse(string)

  implicit def httpResponseToDeserializable(res: HttpResponse): Deserializable =
    new Deserializable(JsonMethods.parse(entityAsString(res)))

  implicit def asJsonString[T](t: T): String = {
    JsonMethods.compact(JsonMethods.render(decompose(t)))
  }

  private def entityAsString(httpResponse: HttpResponse): String = {
    Await.result(httpResponse.entity.toStrict(timeout).map { _.data }.map(_.utf8String), timeout)
  }
}

class Deserializable(jValue: JValue) {
  implicit val formats = DefaultFormats

  def extract[T](implicit m: Manifest[T]): T = {
    jValue.extract[T]
  }
}
