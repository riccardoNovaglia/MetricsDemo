package metricsDemoService

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import metricsDemoService.http.SimpleHttpClient
import metricsDemoService.items._

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.language.{implicitConversions, postfixOps}


object WebServer {
  import akka.event.LoggingAdapter

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val executionContext: ExecutionContext = system.dispatcher

  val log: LoggingAdapter = Logging.getLogger(system, this)

  private val itemsClient = new ItemsClient(new SimpleHttpClient()(system, materializer, executionContext))(system, materializer, executionContext)
  private val itemsRepository: ItemsRepository = new ItemsRepository(itemsClient)(executionContext)

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
