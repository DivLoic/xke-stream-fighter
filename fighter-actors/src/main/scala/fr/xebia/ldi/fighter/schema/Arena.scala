package fr.xebia.ldi.fighter.schema

import com.sksamuel.avro4s.RecordFormat
import fr.xebia.ldi.fighter.entity.ArenaEntity.ArenaEntity

/**
  * Created by loicmdivad.
  */
case class Arena(id: Int, name: String, `type`: String, terminals: Int, location: String)

case object Arena {

  def apply(arena: ArenaEntity): Arena = new Arena(arena.id, arena.name, arena.`type`, arena.terminals, arena.location)

  val arenaFormat: RecordFormat[Arena] = RecordFormat[Arena]
}
