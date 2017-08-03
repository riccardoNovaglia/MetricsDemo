package metricsDemoService.http

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import metricsDemoService.TestActorSystemAndMaterializer
import org.json4s.{jackson, DefaultFormats}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

class ConsumedResponseSpec
  extends FreeSpec
          with Matchers
          with MockFactory
          with TestActorSystemAndMaterializer
          with Json4sSupport
          with ScalaFutures
{
  private implicit val formats = DefaultFormats
  private implicit val serialization = jackson.Serialization

  private val responseBody = "something"
  val httpResponse: HttpResponse = HttpResponse().withEntity(HttpEntity(ContentTypes.`application/json`, responseBody))
  val foo = Foo("aName")
  val fooResponse: HttpResponse = HttpResponse().withEntity(HttpEntity(ContentTypes.`application/json`, """{"name":"aName"}"""))
  private val wrongJson = """{"bar":"foo"}"""
  val notFooResponse: HttpResponse = HttpResponse().withEntity(HttpEntity(ContentTypes.`application/json`, wrongJson))
  val emptyResponse: HttpResponse = HttpResponse().withEntity(HttpEntity.Empty)

  "A consumed response" - {
    "should make the status code available directly" in {
      ConsumedResponse(httpResponse).statusCode should be(200)
    }

    "should make the body available as string" in {
      ConsumedResponse(httpResponse).bodyAsString should be(responseBody)
    }

    "should be able to turn a body into a case class" in {
      whenReady(ConsumedResponse(fooResponse).jsonBodyAs[Foo]) { result =>
        result should be(foo)
      }
    }

    "should return an understandable exception if the unmarshalling fails" in {
      whenReady(ConsumedResponse(notFooResponse).jsonBodyAs[Foo].failed) { ex =>
        ex shouldBe a[JsonMappingException]
        ex.getMessage should be(s"Failed to get [${foo.getClass.getCanonicalName}] from response body '$wrongJson'.")
      }
    }

    "should return an understandable exception if the json is not valid and parsing is attempted" in {
      whenReady(ConsumedResponse(httpResponse).jsonBodyAs[Foo].failed) { ex =>
        ex shouldBe a[InvalidJsonException]
        ex.getMessage should be(s"Failed to get [${foo.getClass.getCanonicalName}] from response body '$responseBody'. Response body is not valid json.")
      }
    }

    "should not fail if the response body is empty" in {
      ConsumedResponse(HttpResponse().withEntity(HttpEntity(ContentTypes.`application/json`, ""))).bodyAsString should be("")
      ConsumedResponse(emptyResponse).bodyAsString should be("")
    }

    "should return and understandable message if the body is empty and parsing is attempted" in {
      whenReady(ConsumedResponse(emptyResponse).jsonBodyAs[Foo].failed) { ex =>
        ex shouldBe a[EmptyResponseException]
        ex.getMessage should be(s"Failed to get [${foo.getClass.getCanonicalName}] from response body. The response body is empty.")
      }
    }

    "should return an understandable message if the response does not provide a Content-Type header and json is required" in {
      val responseWithNoContentType = HttpResponse().withEntity(HttpEntity(responseBody))
      whenReady(ConsumedResponse(responseWithNoContentType).jsonBodyAs[Foo].failed) { ex =>
        ex shouldBe a[UnsupportedContentTypeException]
        ex.getMessage should be(s"Failed to get [${foo.getClass.getCanonicalName}] from response body '$responseBody'. Was expecting 'application/json' but was '${ContentTypes.`text/plain(UTF-8)`}'.")
      }
    }

    "should return an understandable message if the response has a Content-Type header for anything other than json and json is required" in {
      val responseWithNoContentType = HttpResponse().withEntity(HttpEntity(ContentTypes.`text/html(UTF-8)`, responseBody))
      whenReady(ConsumedResponse(responseWithNoContentType).jsonBodyAs[Foo].failed) { ex =>
        ex shouldBe a[UnsupportedContentTypeException]
        ex.getMessage should be(s"Failed to get [${foo.getClass.getCanonicalName}] from response body '$responseBody'. Was expecting 'application/json' but was '${ContentTypes.`text/html(UTF-8)`}'.")
      }
    }
  }
}

case class Foo(name: String)
