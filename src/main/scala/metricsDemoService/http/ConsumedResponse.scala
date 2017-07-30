package metricsDemoService.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.http.scaladsl.unmarshalling.Unmarshaller.NoContentException
import akka.stream.Materializer
import com.fasterxml.jackson.core.JsonParseException
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{jackson, DefaultFormats}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.concurrent.Future.failed
import scala.language.postfixOps


class ConsumedResponse(httpResponse: HttpResponse)
                      (implicit actorSystem: ActorSystem, materializer: Materializer)
  extends Json4sSupport {
  private implicit val formats = DefaultFormats
  private implicit val serialization = jackson.Serialization

  import actorSystem.dispatcher

  val statusCode: Int = httpResponse.status.intValue()
  val bodyAsString: String = Await
    .result(httpResponse.entity.toStrict(300 seconds).map(_.data).map(_.utf8String), 500 milliseconds)

  def jsonBodyAs[ANY]
  (
    implicit um: Unmarshaller[ResponseEntity, ANY],
    ec: ExecutionContext = null,
    mat: Materializer,
    manifest: reflect.Manifest[ANY]
  ): Future[ANY] =
    Unmarshal(httpResponse.entity).to[ANY](um, ec, mat)
      .recoverWith {
        case mappingException: org.json4s.MappingException =>
          failed(UnmarshallingExceptions.mappingException(manifest, bodyAsString, mappingException))
        case parseException: JsonParseException =>
          failed(UnmarshallingExceptions.invalidJson(manifest, bodyAsString, parseException))
        case noContentException: NoContentException.type =>
          failed(UnmarshallingExceptions.noContent(manifest, bodyAsString, noContentException))
      }
}

object UnmarshallingExceptions {
  def mappingException[ANY](classManifest: reflect.Manifest[ANY], actualBody: String, cause: Throwable): JsonMappingException = {
    new JsonMappingException(failureMessageFrom(classManifest, actualBody), cause)
  }

  def invalidJson[ANY](classManifest: reflect.Manifest[ANY], actualBody: String, cause: Throwable): InvalidJsonException = {
    new InvalidJsonException(failureMessageFrom(classManifest, actualBody) + " Response body is not valid json.", cause)
  }

  def noContent[ANY](classManifest: reflect.Manifest[ANY], actualBody: String, cause: Throwable): EmptyResponseException = {
    new EmptyResponseException(s"Failed to get [${classManifest.toString()}] from response body. The response body is empty.", cause)
  }

  private def failureMessageFrom[ANY](classManifest: Manifest[ANY], actualBody: String) = {
    s"Failed to get [${classManifest.toString()}] from response body '$actualBody'."
  }
}

class JsonUnmarshallingException(message: String, cause: Throwable)
  extends RuntimeException(message, cause)

class JsonMappingException(message: String, cause: Throwable)
  extends JsonUnmarshallingException(message, cause)

class InvalidJsonException(message: String, cause: Throwable)
  extends JsonUnmarshallingException(message, cause)

class EmptyResponseException(message: String, cause: Throwable)
  extends JsonUnmarshallingException(message, cause)
