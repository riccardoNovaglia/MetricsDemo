import util.AcceptanceTest

import scala.language.implicitConversions

class ServiceSpec extends AcceptanceTest {

  "The app" - {
    "Should call service A" in {
      whenReady(service.getItems) { result =>
        result.isSuccessful
      }
    }
  }
}
