import util.AcceptanceTest

import scala.language.implicitConversions

class ServiceSpec extends AcceptanceTest {

  "The app" - {
    "Should say hi" in {
      whenReady(service.sayHello) {result =>
        result isSuccessful()
        result contains "Hey"
      }
    }

    "Should call service A" in {
      whenReady(service.getItems) { result =>
        result isSuccessful()
        result contains "[]"
      }
    }
  }
}
