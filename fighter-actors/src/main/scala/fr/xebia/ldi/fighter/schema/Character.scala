package fr.xebia.ldi.fighter.schema

import com.sksamuel.avro4s.RecordFormat
import fr.xebia.ldi.fighter.entity.CharacterEntity.CharacterEntity

/**
  * Created by loicmdivad.
  */
case class Character(name: String, country: String)

case object Character {

  def apply[T](c: CharacterEntity): Character = this (c.name, c.country)

  val fighterFormat: RecordFormat[Character] = RecordFormat[Character]
}
