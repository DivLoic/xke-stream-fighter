package fr.xebia.ldi.fighter.actor

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.ProducerMessage.Message
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.typesafe.config.ConfigFactory
import fr.xebia.ldi.fighter.actor.utils.{Admin, AvroSerde}
import fr.xebia.ldi.fighter.entity.ArenaEntity
import fr.xebia.ldi.fighter.schema._
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

/**
  * Created by loicmdivad.
  */
object Play extends App {

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val logger = LoggerFactory.getLogger(getClass)

  val conf = ConfigFactory.load()

  val avroSerde =  AvroSerde.genericSerdeConfigure(conf)

  AvroSerde.retryCallSchemaRegistry(conf, 3) match {

    case Failure(ex: Throwable) =>
      logger error  "Fail to call the schema registry. shutting down now."
      logger error ex.getMessage
      system.terminate()
      throw ex

    case Success(payload) =>

      logger debug s"Succesfully call the schema registry: $payload"

      val producerSettings: ProducerSettings[String, GenericRecord] =
        ProducerSettings(system, new StringSerializer(), avroSerde.serializer())

      Admin.topicsCreation(conf, producerSettings.properties)

      Source.fromIterator(() => ArenaEntity.Arenas.map(Arena(_)).toIterator)
        .map(arena => new ProducerRecord("ARENAS", arena.id.toString, Arena.arenaFormat.to(arena)))
        .buffer(1, OverflowStrategy.backpressure)
        .runWith(Producer.plainSink(producerSettings))

      implicit val producerRef: ActorRef =
        Source.actorRef[Message[String, GenericRecord, NotUsed]](10, OverflowStrategy.dropBuffer)
        .via(Producer.flow(producerSettings))
        .to(Sink.ignore)
        .run()

      val stores = Stores.allTerminals

      (0 to 8).foreach(i => stores ! s"T$i")
  }

}
