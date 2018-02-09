package fr.xebia.ldi.fighter.entity

/**
  * Created by loicmdivad.
  */
case object ArenaEntity {

  val Arenas: Seq[ArenaEntity] = Seq(`6E-ARR`, `9E-ARR`, `15E-ARR`, `16E-ARR`, DAKAR, NAMUR, ZURICH)

  sealed abstract class ArenaEntity(val id: Int,
                                    val name: String,
                                    val `type`: String,
                                    val terminals: Int,
                                    val location: String)

  case object `6E-ARR` extends ArenaEntity(0, "ARENA 6", "PRO", 30, "PARIS, 6EM ARRONDISSEMENT")
  case object `9E-ARR` extends ArenaEntity(1, "ARENA 9", "FAMILY", 21, "PARIS, 9EM ARRONDISSEMENT")
  case object `15E-ARR` extends ArenaEntity(2, "ARENA 15", "FUN", 15, "PARIS, 15EM ARRONDISSEMENT")
  case object `16E-ARR` extends ArenaEntity(3, "ARENA 16", "FUN", 18, "PARIS, 16EM ARRONDISSEMENT")
  case object DAKAR extends ArenaEntity(4, "ARENA DAKAR", "FAMILY", 45,  "SENEGAL")
  case object NAMUR extends ArenaEntity(5, "ARENA NAMUR", "FUN", 33,  "BELGIUM")
  case object ZURICH extends ArenaEntity(6, "ARENA ZURICH", "PRO", 52,  "SWITZERLAND")
}