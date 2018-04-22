package fr.xebia.ldi.fighter.actor.utils

import java.util.Properties

import com.typesafe.config.Config
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.util.matching.Regex

/**
  * Created by loicmdivad.
  */
object Admin {

  val logger: Logger = LoggerFactory.getLogger(getClass)
  val topicInfoPattern: Regex = """(\S+:\d{1,}:\d{1,})""".r
  val topicNamePattern: Regex = """(^[a-zA-Z0-9]+$)""".r

  def topicsCreation(config: Config, props: Map[String, String]): Unit = {
    val javaProperties = props.toProperties
    val client: AdminClient = AdminClient.create(javaProperties)
    val topics: Vector[NewTopic] = parseTopics(config.getString("admin.topics"))
    client.createTopics(topics.asJava)
    client.close()
  }

  def parseTopics(topics: String): Vector[NewTopic] ={
    topics.split(",").flatMap {
      case topicNamePattern(name) => Some(new NewTopic(name, 1, 1))
      case topicInfoPattern(t) =>
        val params = t.split(":")
        Some(new NewTopic(params.head, params.tail.head.toInt, params.last.toShort))
      case _ => None
    }.toVector
  }

  implicit class propsOps(props: Map[String, String]) {
    def toProperties: Properties = props.foldLeft(new Properties())((properties, map) => {
      properties.put(map._1, map._2)
      properties
    })
  }
}
