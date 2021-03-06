package util

import com.github.tomakehurst.wiremock.client.{MappingBuilder, ResponseDefinitionBuilder}
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, stubFor, urlEqualTo}
import com.github.tomakehurst.wiremock.http.RequestMethod

import scala.language.implicitConversions


case class StubbedEndpointDefinition(method: RequestMethod, endpoint: String) {
  def returnsJson(responseBody: String): Unit =
    stubFor(new MappingBuilder(method, urlEqualTo(endpoint))
        .willReturn(aResponse()
          .withBody(responseBody).withHeader("Content-Type", "application/json")))

  def returns(builder: ResponseDefinitionBuilder): Unit =
    stubFor(new MappingBuilder(method, urlEqualTo(endpoint))
      .willReturn(builder))

}



