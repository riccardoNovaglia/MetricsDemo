package util

import com.github.tomakehurst.wiremock.http.RequestMethod.GET

import scala.language.implicitConversions

trait DependencyAInstance {
  val dependencyA: DependencyA = new DependencyA()
}

class DependencyA() extends StubbedEndpointSugar {
  val defaultReply: String =
    """
      |[
      |  {
      |    "name": "name1",
      |    "id": 123
      |  },
      |  {
      |    "name": "name2",
      |    "id": 321
      |  }
      |]
    """.stripMargin

  val getItems: StubbedEndpointDefinition = GET -> "/getItems"
}
