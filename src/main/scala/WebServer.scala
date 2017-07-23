import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future
import scala.io.StdIn
import scala.language.{implicitConversions, postfixOps}


object WebServer extends JsonSupport with WithActorSystemAndMaterializer {
  import akka.event.LoggingAdapter

  val log: LoggingAdapter = Logging.getLogger(system, this)

  case class Item(name: String, id: Long)
  case class Order(items: List[Item])

  def main(args: Array[String]) {
    val route: Route =
      get {
        pathPrefix("getItems") {
          onSuccess(httpGet("http://localhost:19999/items")) {
            res: HttpResponse =>

              val items = res.extract[List[Item]]
              log.info(items.toString)

              complete(asJsonString(items))
          }
        }
      } ~
      get {
        pathPrefix("hello") {
          complete("Hey")
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  private def httpGet(endpoint: String): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = endpoint))
}
