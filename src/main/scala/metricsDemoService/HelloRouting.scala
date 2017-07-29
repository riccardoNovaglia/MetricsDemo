package metricsDemoService

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.{complete, get, path}

class HelloRouting {
  def sayHi: Route =
    get {
      path("hello") {
        complete("Hey")
      }
    }
}
