import util.{AcceptanceTest, DependencyAInstance}

import scala.language.{implicitConversions, postfixOps}

class ServiceSpec extends AcceptanceTest with DependencyAInstance {

  "The app" - {
    "Should say hi" in {
      whenReady(service.sayHello) { result =>
        result isSuccessful()
        result contains "Hey"
      }
    }

    "Should call dependency A" in {
      dependencyA.getItems returnsJson dependencyA.defaultReply

      whenReady(service.getItems) { result =>
        result isSuccessful()
        result.contains(2, "name")
      }
    }
  }
}
