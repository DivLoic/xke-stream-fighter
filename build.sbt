import com.tapad.docker.DockerComposePlugin.autoImport.{composeFile, dockerImageCreationTask}
import sbt.Keys.version

name := "xke-stream-fighter"

maintainer := "Loïc DIVAD <ldivad@xebia.fr>"

description :=
  """
    |
  """.stripMargin

organizationHomepage := Some(url("http://blog.xebia.fr"))

coverageEnabled := true

val akkaVersion = "2.5.6"
val alpakkaVersion = "0.14"
val reactivStream = "0.17"
val slickVersion = "3.2.1"
val scalaTestVersion = "3.0.4"
val kafkaVersion = "1.0.0"
val cpVerison = "4.0.0"
val jettyVersion = "9.2.22.v20170606"
val jacksonVersion = "2.9.1"

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
val publishSubModule: TaskKey[Unit] = taskKey[Unit]("A docker building task grouping all the submodules")
publishSubModule := {}
//publishSubModule <<= publishSubModule dependsOn (Keys.`package` in Compile)
publishSubModule <<= publishSubModule dependsOn (Seq(
  `fighter-processors`,
  `fighter-actors`
).map(module => (publishLocal in Docker) in module):_*)

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
  )
)

lazy val akkaDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % alpakkaVersion,
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.18",
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

/*lazy val serverDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.eclipse.jetty" % "jetty-server" % jettyVersion,
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion,
    "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion
  )
)*/

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




