package fr.xebia.ldi.fighter.entity

import fr.xebia.ldi.fighter.entity.GameEntity._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen.oneOf

/**
  * Created by loicmdivad.
  */
case object CharacterEntity {

  sealed abstract class CharacterEntity(val name: String,
                                        val game: GameEntity.Value,
                                        val country: String)

  // street fighter
  case object KEN extends CharacterEntity("Ken", StreetFighter, "US")
  case object RYU extends CharacterEntity("Ryu", StreetFighter, "Japan")
  case object GEKI extends CharacterEntity("Geki", StreetFighter, "Japan")
  case object CHUNLI extends CharacterEntity("Chun-Li", StreetFighter, "China")
  case object AKUMA extends CharacterEntity("Akuma", StreetFighter, "Japan")
  case object SAKURA extends CharacterEntity("Sakura", StreetFighter, "Japan")
  case object DHALSIM extends CharacterEntity("Dhalsim", StreetFighter, "India")
  case object BLAIR extends CharacterEntity("Blair", StreetFighter, "UK")

  val StreetCast = Seq(KEN, RYU, GEKI, CHUNLI, AKUMA, SAKURA, DHALSIM, BLAIR)

  implicit lazy val StreetArbitrary: Arbitrary[CharacterEntity] = Arbitrary(oneOf(StreetCast))

  // takken
  case object JIN extends CharacterEntity("Jin", Takken, "Japan")
  case object ASUKA extends CharacterEntity("Asuka", Takken, "Japan")
  case object EMILIE extends CharacterEntity("Emilie", Takken, "Monaco")
  case object KAZUYA extends CharacterEntity("Kazuya", Takken, "Japan")

  val TekkenCast = Seq(JIN, ASUKA, EMILIE, KAZUYA)

  implicit lazy val TakkenaArbitrary: Arbitrary[CharacterEntity] = Arbitrary(oneOf(TekkenCast))

  // king of fighter
  case object MAI extends CharacterEntity("Mai", KingOfFighters, "Japan")
  case object RAMON extends CharacterEntity("Ramon", KingOfFighters, "Mexico")
  case object NELSON extends CharacterEntity("Nelson", KingOfFighters, "Brazil")
  case object VANESSA extends CharacterEntity("Vanessa", KingOfFighters, "France")

  val KoFCast = Seq(MAI, RAMON, NELSON, VANESSA)

  implicit lazy val KoFArbitrary: Arbitrary[CharacterEntity] = Arbitrary(oneOf(KoFCast))

  // soul calibur
  case object KILIK extends CharacterEntity("Kilik", SoulCalibur, "China")
  case object IVY extends CharacterEntity("Ivy", SoulCalibur, "UK")
  case object SIEGFRIED extends CharacterEntity("Siegfried", SoulCalibur, "HRE")
  case object NIGHTMARE extends CharacterEntity("Nightmare", SoulCalibur, "")

  val SoulCast = Seq(KILIK, IVY, SIEGFRIED, NIGHTMARE)

  implicit lazy val SoulArbitrary: Arbitrary[CharacterEntity] = Arbitrary(oneOf(SoulCast))
}
