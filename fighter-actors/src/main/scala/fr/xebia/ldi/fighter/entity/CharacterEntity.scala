package fr.xebia.ldi.fighter.entity

import org.scalacheck.Arbitrary
import org.scalacheck.Gen.oneOf

/**
  * Created by loicmdivad.
  */
case object CharacterEntity {

  val Characters = Seq(KEN, RYU, GEKI, CHUNLI, AKUMA, SAKURA, DHALSIM, BLAIR)

  implicit lazy val arbitraryCharacter: Arbitrary[CharacterEntity] = Arbitrary(oneOf(Characters))

  sealed abstract class CharacterEntity(val name: String,
                                        val country: String,
                                        val specialty: String,
                                        val mention: String) {

  }

  case object KEN extends CharacterEntity("Ken", "US", "", "2")
  case object RYU extends CharacterEntity("Ryu", "Japan", "", "6")
  case object GEKI extends CharacterEntity("Geki", "Japan", "", "6")
  case object CHUNLI extends CharacterEntity("Chun-Li", "China", "", "3")
  case object AKUMA extends CharacterEntity("Akuma", "Japan", "", "7")
  case object SAKURA extends CharacterEntity("Sakura", "Japan", "", "6")
  case object DHALSIM extends CharacterEntity("Dhalsim", "India", "", "2")
  case object BLAIR extends CharacterEntity("Blair", "UK", "", "2")

}
