package fr.xebia.ldi.fighter.actor.utils

import java.util.Properties

import com.typesafe.config.Config
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}

import scala.collection.JavaConverters._

/**
  * Created by loicmdivad.
  */
object Admin {

  def topicsCreation(config: Config, props: Map[String, String]): Unit = {

    val javaProperties = props.foldLeft(new Properties())((properties, map) => {
      properties.put(map._1, map._2)
      properties
    })

    val client: AdminClient = AdminClient.create(javaProperties)

    val topics: List[NewTopic] = config
      .getString("admin.topics")
      .split(",")
      .map(_.split(":"))
      .map(t => new NewTopic(t.head, t(1).toInt, t(2).toShort))
      .toList

    client.createTopics(topics.asJava)
  }

}
