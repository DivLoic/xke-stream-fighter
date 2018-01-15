package fr.xebia.ldi.fighter.actor

import akka.actor.ActorDSL.{actor, _}
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.ProducerMessage.Message
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.{Done, NotUsed}
import com.typesafe.config.ConfigFactory
import fr.xebia.ldi.fighter.entity.ArenaEntity
import fr.xebia.ldi.fighter.entity.ArenaEntity._
import fr.xebia.ldi.fighter.schema.Arena
import fr.xebia.ldi.fighter.actor.utils.{Admin, AvroSerde}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by loicmdivad.
  */
object Play extends App {

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val logger = LoggerFactory.getLogger(getClass)

  val conf = ConfigFactory.load()

  val localGeneriAvroSerde =  AvroSerde.genericSerdeConfigure(conf)

  AvroSerde.retryCallSchemaRegistry(conf, 3) match {
    case Failure(ex: Throwable) =>
      logger error  "Fail to call the schema registry. shuting down now."
      logger error ex.getMessage
      system.terminate()
      throw ex

    case Success(payload) =>

      val producerSettings: ProducerSettings[String, GenericRecord] =
        ProducerSettings(system, new StringSerializer(), localGeneriAvroSerde.serializer())

      Admin.topicsCreation(conf, producerSettings.properties)

      val doneRooms: Future[Done] = Source.fromIterator(() => {
        ArenaEntity.Arenas.map(Arena(_))
          .map(arena => new ProducerRecord("ARENAS", arena.id.toString, Arena.arenaFormat.to(arena)))
          .toIterator
      })
        .buffer(1, OverflowStrategy.backpressure)
        .runWith(Producer.plainSink(producerSettings))


      val producerRef: ActorRef = Source.actorRef[Message[String, GenericRecord, NotUsed]](10, OverflowStrategy.dropBuffer)
        .via(Producer.flow(producerSettings))
        .to(Sink.ignore)
        .run()

      val master = actor(new Act {

        var executors: Map[String, ActorRef] = Map[String, ActorRef]()

        whenStarting {
          executors = executors ++ Seq(
            "T0" -> Terminal.actors(0, `6E-ARR`, producerRef),
            "T1" -> Terminal.actors(1, `9E-ARR`, producerRef),
            "T2" -> Terminal.actors(2, `15E-ARR`, producerRef),
            "T3" -> Terminal.actors(3, `16E-ARR`, producerRef),
            "T4" -> Terminal.actors(4, DAKAR, producerRef),
            "T5" -> Terminal.actors(5, NAMUR, producerRef),
            "T6" -> Terminal.actors(6, ZURICH, producerRef))
        }

        become {
          case name: String => executors(name) ! "Go!"
          case _ =>
        }

      })

      master ! "T0"
      master ! "T1"
      master ! "T2"
      master ! "T3"
      master ! "T4"
      master ! "T5"
      master ! "T6"

  }


}
