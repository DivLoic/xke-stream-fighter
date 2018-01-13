package fr.xebia.ldi.fighter.schema

import com.sksamuel.avro4s.RecordFormat
import fr.xebia.ldi.fighter.entity.CharacterEntity.CharacterEntity

/**
  * Created by loicmdivad.
  */
case class Player(name: String,
                  life: Int,
                  combo: Int,
                  fatal_hit: Option[Double] = None)

case object Player {

  def apply(c: CharacterEntity): Player = this(c.name, 100, 0)

  def apply(c: CharacterEntity, life: Int, combo: Int, hits: Double): Player = this(c.name, life, combo, Some(hits))

  val playerFormat: RecordFormat[Player] = RecordFormat[Player]

}