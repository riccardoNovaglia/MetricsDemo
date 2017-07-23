package util

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
    WireMock.reset ()
  }
}
