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
  val httpResponse = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, responseBody))
  val foo = Foo("aName")
  val fooResponse = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, """{"name":"aName"}"""))
  private val wrongJson = """{"bar":"foo"}"""
  val notFooResponse = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, wrongJson))
  val emptyResponse = HttpResponse(entity = HttpEntity.Empty)

  "A consumed response" - {
    "should make the status code available directly" in {
      new ConsumedResponse(httpResponse).statusCode should be(200)
    }

    "should make the body available as string" in {
      new ConsumedResponse(httpResponse).bodyAsString should be(responseBody)
    }

    "should be able to turn a body into a case class" in {
      whenReady(new ConsumedResponse(fooResponse).jsonBodyAs[Foo]) { result =>
        result should be(foo)
      }
    }

    "should return an understandable exception if the unmarshalling fails" in {
      whenReady(new ConsumedResponse(notFooResponse).jsonBodyAs[Foo].failed) { ex =>
        ex shouldBe a[JsonMappingException]
        ex.getMessage should be(s"Failed to get [${foo.getClass.getCanonicalName}] from response body '$wrongJson'.")
      }
    }

    "should return an understandable exception if the json is not valid and parsing is attempted" in {
      whenReady(new ConsumedResponse(httpResponse).jsonBodyAs[Foo].failed) { ex =>
        ex shouldBe a[InvalidJsonException]
        ex.getMessage should be(s"Failed to get [${foo.getClass.getCanonicalName}] from response body '$responseBody'. Response body is not valid json.")
      }
    }

    "should not fail if the response body is empty" in {
      new ConsumedResponse(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, ""))).bodyAsString should be("")
      new ConsumedResponse(emptyResponse).bodyAsString should be("")
    }

    "should return and understandable message if the body is empty and parsing is attempted" in {
      whenReady(new ConsumedResponse(emptyResponse).jsonBodyAs[Foo].failed) { ex =>
        ex shouldBe a[EmptyResponseException]
        ex.getMessage should be(s"Failed to get [${foo.getClass.getCanonicalName}] from response body. The response body is empty.")
      }
    }
  }
}

case class Foo(name: String)
