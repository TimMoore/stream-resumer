organization in ThisBuild := "com.lightbend"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

// change port numbers to avoid conflict with chirper
lagomServiceLocatorPort in ThisBuild := 8008
lagomServiceGatewayPort in ThisBuild := 9009

lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

// allow locating the chirp service
lagomUnmanagedServices in ThisBuild := Map("chirpservice" -> "http://localhost:9000")

lazy val `stream-resumer` = (project in file("."))
  .aggregate(`stream-resumer-api`, `stream-resumer-impl`)

lazy val `stream-resumer-api` = (project in file("stream-resumer-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi
    )
  )

lazy val `stream-resumer-impl` = (project in file("stream-resumer-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      "sample.chirper" %% "chirp-api" % "1.0-SNAPSHOT"
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`stream-resumer-api`)

def common = Seq(
  javacOptions in compile += "-parameters"
)

