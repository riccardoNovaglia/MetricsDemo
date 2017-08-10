package metricsDemoService.http

import akka.http.scaladsl.Http
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.{ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.typesafe.config.{Config, ConfigFactory}
import metricsDemoService.TestActorSystemAndMaterializer
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}


class SimpleHttpClientSpec
  extends fixture.FreeSpec
          with Matchers
          with ScalaFutures
          with TestActorSystemAndMaterializer
          with MockFactory
          with WiremockFixture
{
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val config: Config = ConfigFactory.parseString(
    """
      |timeoutInMs: 10
    """.stripMargin)
  val httpClientConfig = new HttpClientConfig(config)

  val client = new SimpleHttpClient(httpClientConfig, Http(), null)
  val somePath = "/somePath"
  val someBodyString = "hey"
  val someResponse: ResponseDefinitionBuilder = aResponse().withStatus(200).withBody(someBodyString)
  val someUrl = s"http://localhost:$port$somePath"
  val millisecondsTimeout = 10

  "The Simple Http Client" - {
    "should send requests and get responses" in { () =>
      stubFor(get(urlEqualTo(somePath)).willReturn(someResponse))

      whenReady(client.get(someUrl)) { response =>
        response.statusCode should be(200)
        response.bodyAsString should be(someBodyString)
      }
    }

    "Should throw an exception if the call times out" ignore { () =>
      stubFor(get(urlEqualTo(somePath)).willReturn(someResponse.withFixedDelay(10000)))

      whenReady(client.get(someUrl).failed) { ex =>
        ex shouldBe a[HttpTimeoutException]
        ex.getMessage should be(s"$someUrl did not respond within $millisecondsTimeout milliseconds. Aborting")
      }
    }
  }
}

trait WiremockFixture { this: fixture.TestSuite =>

  val port = 19999

  override def withFixture(test: OneArgTest): Outcome = this.withFixture(test)

  override def withFixture(test: NoArgTest): Outcome = {
    WireMock.configureFor("localhost", port)
    val wiremockServer = new WireMockServer(port)
    wiremockServer.start()
    val outcome = test()
    wiremockServer.stop()
    outcome
  }
}
