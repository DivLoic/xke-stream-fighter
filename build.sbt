import com.tapad.docker.DockerComposePlugin.autoImport.{composeFile, dockerImageCreationTask}
import sbt.Keys.{publishLocal, version}

name := "xke-stream-fighter"

maintainer := "Loïc DIVAD <ldivad@xebia.fr>"

description :=
  """
    |A simple demo of the kafka-streams Processor API
  """.stripMargin

organizationHomepage := Some(url("http://blog.xebia.fr"))

coverageEnabled := true

val akkaVersion = "2.5.9"
val reactivStream = "0.19"
val slickVersion = "3.2.1"
val scalaTestVersion = "3.0.4"
val kafkaVersion = "1.0.1"
val cpVerison = "4.0.0"

lazy val common = Seq(

  version := "0.1.0-SNAPSHOT",

  isSnapshot := false,

  scalaVersion := "2.12.4",

  organization := "fr.xebia",

  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.1",
    "joda-time" % "joda-time" % "2.9.7",
    "com.sksamuel.avro4s" % "avro4s-core_2.12" % "1.8.0",
    "ch.qos.logback" % "logback-classic" % "1.2.0" force()
  ),

  logLevel in doc := Level.Error
)

val publishSubModule: TaskKey[Unit] =
  taskKey[Unit]("Submodules docker building task")

publishSubModule := {
  (publishLocal in Docker in `fighter-actors`).value
  (publishLocal in Docker in `fighter-processors`).value
}

lazy val `xke-stream-fighter` = (project in file("."))
  .aggregate(`fighter-processors`, `fighter-actors`)
  .enablePlugins(DockerComposePlugin)
  .settings(dockerComposeSettings: _*)

lazy val `fighter-processors` = project
  .settings(common: _*)
  .settings(kafkaDependencies: _*)
  .settings(avroGeneratorSettings: _*)
  .enablePlugins(JavaAppPackaging, DockerPlugin, DockerComposePlugin)
  .settings(dockerSettings ++ (packageName in Docker := "fighter-processors") : _*)

lazy val `fighter-actors` = project
  .settings(common: _*)
  .settings(akkaDependencies: _*)
  .settings(kafkaDependencies: _*)
  .settings(mathDependencies: _*)
  .enablePlugins(JavaAppPackaging, DockerPlugin, DockerComposePlugin)
  .settings(dockerSettings ++ (packageName in Docker := "fighter-actors") : _*)

lazy val kafkaDependencies = Seq(
  resolvers ++= Seq("confluent" at "http://packages.confluent.io/maven/"),

  libraryDependencies ++= Seq(
    "org.apache.kafka" % "kafka-clients" % kafkaVersion,
    "org.apache.kafka" % "kafka-streams" % kafkaVersion,
    "io.confluent" % "kafka-avro-serializer" % cpVerison,
    "io.confluent" % "kafka-streams-avro-serde" % cpVerison

  ).map(_ exclude("org.slf4j", "slf4j-log4j12"))
)

lazy val akkaDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.18" exclude("org.apache.kafka", "kafka-clients"),
    "org.slf4j" % "slf4j-nop" % "1.7.25" exclude("org.slf4j", "slf4j-log4j12")
  )
)

lazy val mathDependencies = Seq(
  resolvers += "Sonatype Releases" at
    "https://oss.sonatype.org/content/repositories/releases/",

  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4",
    "org.scalanlp" %% "breeze" % "0.13.2"
  )
)

lazy val dockerSettings = Seq(

  version in Docker := version.value,

  dockerBaseImage in Docker := "java:8-jdk-alpine",

  maintainer in Docker := "Loïc DIVAD <ldivad@xebia.fr>",

  mappings in Docker += (baseDirectory.value / "/src/main/resources/") -> "resources"

)

lazy val dockerComposeSettings = Seq(

  composeFile := "./docker-compose.yml",

  dockerImageCreationTask := publishSubModule.value
)

lazy val avroGeneratorSettings = Seq(

  javaSource in AvroConfig := sourceManaged.value / "generated",

  managedSourceDirectories in Compile += sourceManaged.value / "generated",

  sourceDirectory in AvroConfig := (resourceDirectory in Compile).value / "avro"
)