package fr.xebia.ldi.fighter.schema

import com.sksamuel.avro4s.{AvroSchema, RecordFormat}
import org.apache.avro.Schema

/**
  * Created by loicmdivad.
  */
case class Victory(character: Player, arena: Arena)

case object Victory {

  val victoryFormat: RecordFormat[Victory] = RecordFormat[Victory]

  val victorySchema: Schema = AvroSchema[Victory]
}
