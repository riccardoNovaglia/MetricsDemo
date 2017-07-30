package metricsDemoService

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.ExecutionContextExecutor

trait TestActorSystemAndMaterializer extends Suite with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  override protected def afterAll(): Unit = {
    system.terminate()
    materializer.shutdown()
  }
}
