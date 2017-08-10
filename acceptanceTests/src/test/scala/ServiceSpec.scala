import util.{AcceptanceTest, DependencyAInstance, Magic}

import scala.language.{implicitConversions, postfixOps}

class ServiceSpec extends AcceptanceTest with DependencyAInstance with Magic {

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

    "Should call dependency A - ALT" in {
      service.getItems {
        dependencyA.getItems returnsJson dependencyA.defaultReply
      } Then { implicit result =>
        isSuccessful
        contains(2, "name")
      }
    }

    "Should call dependency A - ALT2" in {
      service.getItems1 Given {
        dependencyA.getItems returnsJson dependencyA.defaultReply
      } Then { implicit result =>
        isSuccessful
        contains(2, "name")
      }
    }
  }
}
