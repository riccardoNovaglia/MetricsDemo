package util

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

trait SystemAndMaterializer {
  implicit val actorSystem: ActorSystem = ActorSystem("acceptance-tests")
  implicit val materializer: Materializer = ActorMaterializer()
}
