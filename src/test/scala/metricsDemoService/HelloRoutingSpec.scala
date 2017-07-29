package metricsDemoService

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}

class HelloRoutingSpec
  extends FreeSpec
          with Matchers
          with MockFactory
          with ScalatestRouteTest {

  val routingService: HelloRouting = new HelloRouting()

  "The Routing Service" - {
    "should return some stuff when asked to say hi" in {
      Get("/hello") ~> routingService.sayHi ~> check {
        responseAs[String] should be("Hey")
      }
    }
  }
}
