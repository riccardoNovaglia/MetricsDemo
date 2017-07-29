package util

import com.typesafe.config.{Config, ConfigFactory}

trait TestsConfiguration {

  val config: Config = ConfigFactory.parseResources("acceptance-tests.conf").resolve()

  val serviceConfiguration: ServiceConfiguration = new ServiceConfiguration(config.getConfig("service"))
}
