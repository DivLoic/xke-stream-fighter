package fr.xebia.ldi.fighter.schema

import com.sksamuel.avro4s.RecordFormat
import fr.xebia.ldi.fighter.entity.GameEntity

/**
  * Created by loicmdivad.
  */
case class Round(arena: Int, terminal: Int, winner: Player, looser: Player, game: GameEntity.Value, timestamp: Long)

case object Round {
  val roundFormat: RecordFormat[Round] = RecordFormat[Round]
}
