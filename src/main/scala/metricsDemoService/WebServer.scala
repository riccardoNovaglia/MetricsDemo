package metricsDemoService

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.codahale.metrics.{ConsoleReporter, MetricRegistry, Timer}
import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import com.typesafe.config.Config
import metricsDemoService.http.{HttpClientConfig, SimpleHttpClient}
import metricsDemoService.items._
import metricsDemoService.util.Actors

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.language.{implicitConversions, postfixOps}

object WebServer {
  import akka.event.LoggingAdapter

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val actors = new Actors(system, materializer, executionContext)

  val config: Config = system.settings.config

  private val metrics = new MetricRegistry()
  consoleReporter
  graphiteReporter(metrics)
  val timer: Timer = metrics.timer("requests")

  val log: LoggingAdapter = Logging.getLogger(system, this)
  private val client = new SimpleHttpClient(new HttpClientConfig(config), Http(), timer: Timer)
  private val itemsClient = new ItemsClient(client) // TODO: fill config
  private val itemsRepository: ItemsRepository = new ItemsRepository(itemsClient)

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

  private def consoleReporter = {
    val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS).build
    reporter.start(1, TimeUnit.SECONDS)
  }

  private def graphiteReporter(registry: MetricRegistry) = {
    import java.util.concurrent.TimeUnit

    import com.codahale.metrics.MetricFilter
    val graphite = new Graphite(new InetSocketAddress("localhost", 2003))
    val reporter = GraphiteReporter.forRegistry(registry).prefixedWith("web1.example.com")
      .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).filter(MetricFilter.ALL)
      .build(graphite)
    reporter.start(2, TimeUnit.SECONDS)
  }
}
