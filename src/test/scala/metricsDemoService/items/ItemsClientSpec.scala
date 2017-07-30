package metricsDemoService.items

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import metricsDemoService.TestActorSystemAndMaterializer
import metricsDemoService.http.ConsumedResponse
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future.{failed, successful}

class ItemsClientSpec
  extends FreeSpec
          with Matchers
          with MockFactory
          with ScalaFutures
          with TestActorSystemAndMaterializer
{
  implicit val formats = DefaultFormats

  val httpClient: SimpleHttpClient = stub[SimpleHttpClient]
  val itemsClient = new ItemsClient(httpClient)

  val itemsList = List(Item("name", 123), Item("anotherName", 321))
  val itemsAsJson: String = write(itemsList)

  "The Items client" - {
    "should call the items service to get a list of all items" in {
      val response = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, itemsAsJson))
      httpClient.get _ when "getItems" returns successful(new ConsumedResponse(response))

      whenReady(itemsClient.getAllItems) { result =>
        result should be(itemsList)
      }
    }

    "should throw an exception if no items are returned" in {
      val response = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, "[]"))
      httpClient.get _ when "getItems" returns successful(new ConsumedResponse(response))

      whenReady(itemsClient.getAllItems.failed) { ex =>
        ex shouldBe a[NoItemsReturnedException]
      }
    }

    "should return an exception if the http client fails" in {
      httpClient.get _ when "getItems" returns failed(new RuntimeException("something"))

      whenReady(itemsClient.getAllItems.failed) { ex =>
        ex shouldBe a[ItemsClientFailureException]
      }
    }

    "should return an exception if the response cannot be turned into items" in {
      val response = HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, """{"bar":"foo"}"""))
      httpClient.get _ when "getItems" returns successful(new ConsumedResponse(response))

      whenReady(itemsClient.getAllItems.failed) { ex =>
        ex shouldBe a[ItemsClientFailureException]
      }
    }
  }
}
