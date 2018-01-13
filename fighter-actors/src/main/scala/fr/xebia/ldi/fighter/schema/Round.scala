package fr.xebia.ldi.fighter.schema

import com.sksamuel.avro4s.RecordFormat
import fr.xebia.ldi.fighter.entity.FieldEntity.Value

/**
  * Created by loicmdivad.
  */
case class Round(arena: Int,
                 terminal: Int,
                 winner: Player,
                 looser: Player,
                 field: Value,
                 id: Option[String] = None)

case object Round {

  val roundFormat: RecordFormat[Round] = RecordFormat[Round]

}
