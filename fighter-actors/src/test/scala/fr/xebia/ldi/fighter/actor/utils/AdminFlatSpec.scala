package fr.xebia.ldi.fighter.actor.utils

import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

/**
  * Created by loicmdivad.
  */
class AdminFlatSpec extends FlatSpec  with Matchers with GivenWhenThen {

  "parseTopics" should "create instance of NewTopics" in {
    Given("a seperated list of topics")
    val topics = "topicA:6:3,TOPICB,topicc:16:2,/ù%$*@"

    When("parseTopics is applied")
    val newTopics = Admin.parseTopics(topics)

    Then("it returns a vector of new topics")
    newTopics should have length 3

    newTopics.head.name() shouldBe "topicA"
    newTopics.tail.head.name() shouldBe "TOPICB"
    newTopics.last.name() shouldBe "topicc"

    newTopics.head.numPartitions() shouldBe 6
    newTopics.tail.head.numPartitions() shouldBe 1
    newTopics.last.numPartitions() shouldBe 16

    newTopics.head.replicationFactor() shouldBe 3
    newTopics.tail.head.replicationFactor() shouldBe 1
    newTopics.last.replicationFactor() shouldBe 2
  }

}
