package util

import org.scalatest.{FreeSpec, Matchers}
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
          with SystemAndMaterializer {
  implicit val defaultPatience = PatienceConfig(timeout = Span(2, Seconds), interval = Span(100, Millis))
}
