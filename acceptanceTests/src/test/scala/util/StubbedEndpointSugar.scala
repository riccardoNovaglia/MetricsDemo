package util

import com.github.tomakehurst.wiremock.http.RequestMethod

import scala.language.implicitConversions


trait StubbedEndpointSugar {
  implicit def methodAndEndpointToStubbedEndpointDefinition(methAndEnd: (RequestMethod, String))(implicit dependencyName: String): StubbedEndpointDefinition =
    StubbedEndpointDefinition(methAndEnd._1, methAndEnd._2, dependencyName)
}
