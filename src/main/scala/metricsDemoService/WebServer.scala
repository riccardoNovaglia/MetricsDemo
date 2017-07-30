package metricsDemoService

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import metricsDemoService.items.{Item, ItemsRepository, ItemsRouting}

import scala.io.StdIn
import scala.language.{implicitConversions, postfixOps}


object WebServer extends ActorSystemAndMaterializer {
  import akka.event.LoggingAdapter

  val log: LoggingAdapter = Logging.getLogger(system, this)

  private val itemsRepository: ItemsRepository = new ItemsRepository(null)

  private val helloRouting: HelloRouting = new HelloRouting()
  private val itemsRouting: ItemsRouting = new ItemsRouting(itemsRepository)

  case class Order(items: List[Item])

  def main(args: Array[String]) {
    val route: Route =
      helloRouting.sayHi    ~
      itemsRouting.getItems

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
