name := "sample-akka-http-authenticator"
organization := "com.qtomerr"
version := "0.0.1"
description := "Generic authentication server for user management and session instantiation"

// Scala version limited due to Quill support: https://github.com/getquill/quill/pull/617
scalaVersion := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "JBoss" at "https://repository.jboss.org/"

libraryDependencies ++= {
    val akkaV = "2.5.1"
    val akkaHttpV = "10.0.6"
    val scalaTestV = "3.0.3"
    Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-stream" % akkaV,
        "com.typesafe.akka" %% "akka-testkit" % akkaV,
        "com.typesafe.akka" %% "akka-http" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
        "org.scalatest" %% "scalatest" % scalaTestV % "test",
        "mysql" % "mysql-connector-java" % "5.1.38",
        "io.getquill" %% "quill-jdbc" % "1.1.1",
        "com.github.nscala-time" %% "nscala-time" % "2.16.0",
        "net.debasishg" %% "redisclient" % "3.4",
        "org.mindrot" % "jbcrypt" % "0.4"
    )
}
// For quill's probing functionality
unmanagedClasspath in Compile += baseDirectory.value / "src" / "main" / "resources"

// SBT-assembly plugin options
mainClass in assembly := Some("authenticator.api.AuthenticatorServer")
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true)

Revolver.settings
