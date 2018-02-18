package fr.xebia.ldi.fighter.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import fr.xebia.ldi.fighter.entity.ArenaEntity._
import fr.xebia.ldi.fighter.schema.{KingOfFigtersGame, SoulCaliburGame, StreetFighterGame, TakkenGame}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try

/**
  * Created by loicmdivad.
  */
case class Stores(games: Map[String, ActorRef]) extends Actor {

  def logger: Logger = LoggerFactory.getLogger(getClass)

  override def receive: Receive = {
    case name: String => Try(games(name) ! "Ready? Fight!")
    case _ => logger debug "Receive something different than a terminal name"
  }

}

case object Stores {

  def allTerminals(implicit system: ActorSystem, publisher: ActorRef): ActorRef = {
    val map: Map[String, ActorRef] = Map(
      "T0" -> Terminal.actors(0, TakkenGame, `6E-ARR`, publisher),
      "T1" -> Terminal.actors(1, SoulCaliburGame, `6E-ARR`, publisher),
      "T2" -> Terminal.actors(2, KingOfFigtersGame, `6E-ARR`, publisher),
      "T3" -> Terminal.actors(3, KingOfFigtersGame, `9E-ARR`, publisher),
      "T4" -> Terminal.actors(4, StreetFighterGame, `15E-ARR`, publisher),
      "T5" -> Terminal.actors(5, StreetFighterGame, `16E-ARR`, publisher),
      "T6" -> Terminal.actors(6, StreetFighterGame, DAKAR, publisher),
      "T7" -> Terminal.actors(7, StreetFighterGame, NAMUR, publisher),
      "T8" -> Terminal.actors(8, StreetFighterGame, ZURICH, publisher)
    )

    system.actorOf(Props.apply(classOf[Stores], map))
  }

}
