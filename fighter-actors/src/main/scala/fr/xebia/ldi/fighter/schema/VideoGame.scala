package fr.xebia.ldi.fighter.schema

import fr.xebia.ldi.fighter.entity.CharacterEntity.CharacterEntity
import fr.xebia.ldi.fighter.entity.{CharacterEntity, GameEntity}
import fr.xebia.ldi.fighter.entity.GameEntity.Game
import org.scalacheck.Arbitrary

/**
  * Created by loicmdivad.
  * TODO: try to replace this with type class
  */
sealed trait VideoGame {
  def label: Game

  def genarator: Arbitrary[CharacterEntity]
}

case object StreetFighterGame extends VideoGame {
  override def label: Game = GameEntity.StreetFighter

  override def genarator: Arbitrary[CharacterEntity] = CharacterEntity.StreetArbitrary
}

case object TakkenGame extends VideoGame {
  override def label: Game = GameEntity.Takken

  override def genarator: Arbitrary[CharacterEntity] = CharacterEntity.TakkenaArbitrary
}

case object KingOfFigtersGame extends VideoGame {
  override def label: Game = GameEntity.KingOfFighters

  override def genarator: Arbitrary[CharacterEntity] = CharacterEntity.KoFArbitrary
}

case object SoulCaliburGame extends VideoGame {
  override def label: Game = GameEntity.SoulCalibur

  override def genarator: Arbitrary[CharacterEntity] = CharacterEntity.SoulArbitrary
}