package fr.xebia.ldi.fighter.schema

import fr.xebia.ldi.fighter.entity.CharacterEntity.CharacterEntity
import fr.xebia.ldi.fighter.entity.{CharacterEntity, GameEntity}
import fr.xebia.ldi.fighter.entity.GameEntity.Game
import org.scalacheck.Arbitrary

/**
  * Created by loicmdivad.
  */
sealed trait VideoGame {

  def label: Game = this match {
    case TakkenGame => GameEntity.Takken
    case SoulCaliburGame => GameEntity.SoulCalibur
    case StreetFighterGame => GameEntity.StreetFighter
    case KingOfFigtersGame => GameEntity.KingOfFighters
  }

  def genarator: Arbitrary[CharacterEntity] = this match {
    case TakkenGame => CharacterEntity.TakkenaArbitrary
    case SoulCaliburGame => CharacterEntity.SoulArbitrary
    case StreetFighterGame => CharacterEntity.StreetArbitrary
    case KingOfFigtersGame => CharacterEntity.KoFArbitrary
  }

}

case object StreetFighterGame extends VideoGame
case object TakkenGame extends VideoGame
case object KingOfFigtersGame extends VideoGame
case object SoulCaliburGame extends VideoGame