package metricsDemoService.util

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

class Actors(_actorSystem: ActorSystem, _materializer: Materializer, _executionContext: ExecutionContext) {
  implicit val actorSystem: ActorSystem = _actorSystem
  implicit val materializer: Materializer = _materializer
  implicit val executionContext: ExecutionContext =  _executionContext
}
