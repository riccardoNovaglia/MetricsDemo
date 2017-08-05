name := "MetricsDemo"

version := "1.0"

scalaVersion := "2.12.2"
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies := Seq(
  "io.dropwizard.metrics" % "metrics-core" % "3.2.3",
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "org.json4s" %% "json4s-native" % "3.5.2",
  "org.json4s" %% "json4s-jackson" % "3.5.2",
  "de.heikoseeberger" %% "akka-http-json4s" % "1.17.0",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalamock" % "scalamock-scalatest-support_2.12" % "3.6.0" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.7" % "test",
  "com.github.tomakehurst" % "wiremock" % "1.18" % "test"
)

val ac = (project in file("acceptanceTests"))
  .settings(
    scalaVersion := "2.12.2",
    libraryDependencies := Seq(
      "org.scalatest" %% "scalatest" % "3.0.1",
      "com.github.tomakehurst" % "wiremock" % "1.18",
      "com.typesafe.akka" %% "akka-http" % "10.0.9",

      "org.json4s" %% "json4s-native" % "3.5.2",
      "org.json4s" %% "json4s-jackson" % "3.5.2"
    )
  )
