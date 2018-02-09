package fr.xebia.ldi.fighter.actor.utils

import com.typesafe.config.Config
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * Created by loicmdivad.
  */
object AvroSerde {

  val kafkaClientKey = "akka.kafka.producer.kafka-clients"
  val registryKey = "schema.registry.url"
  lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  def genericSerdeConfigure(conf: Config): GenericAvroSerde = {
    val prop = Map(registryKey -> conf.getConfig(kafkaClientKey).getString(registryKey)).asJava
    val genericAvroSerde = new GenericAvroSerde()
    genericAvroSerde.configure(prop, false)
    genericAvroSerde
  }

  @tailrec
  def retryCallSchemaRegistry(conf: Config, countdown: Int): Try[String] = {
    val registryUrl = conf.getString(s"$kafkaClientKey.$registryKey")
    Try(scala.io.Source.fromURL(registryUrl).mkString) match {
      case result: Success[String] =>
        logger info "Successfully call the Schema Registry."
        result
      case result: Failure[String] if countdown <= 0 =>
        logger error "Fail to call the Schema Registry for the last time."
        result
      case result: Failure[String] if countdown > 0 =>
        logger error "Fail to call the Schema Registry, retry in 15 secs."
        Thread.sleep(15.second.toMillis)
        retryCallSchemaRegistry(conf, countdown -1)
    }
  }
}
