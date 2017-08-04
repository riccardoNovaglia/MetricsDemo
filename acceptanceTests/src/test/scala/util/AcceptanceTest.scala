package util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest.{BeforeAndAfter, FreeSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.language.implicitConversions

trait AcceptanceTest
  extends FreeSpec
          with Matchers
          with ScalaFutures
          with TestsConfiguration
          with ServiceInstance
          with ServiceResponseVerification
          with SystemAndMaterializer
          with BeforeAndAfter {

  implicit val defaultPatience = PatienceConfig(timeout = Span(2, Seconds), interval = Span(100, Millis))

  WireMock.configureFor("localhost", 19999)

  before {
    Wiremock.start()
    WireMock.reset()
  }
}

object Wiremock {
  var wiremockServer: WireMockServer = _

  def start(): Unit = {
    if (wiremockServer == null) {
      wiremockServer = new WireMockServer(19999)
      wiremockServer.start()
    }
  }
}
