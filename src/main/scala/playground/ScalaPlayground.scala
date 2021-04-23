package playground

import akka.actor.ActorSystem

object ScalaPlayground extends App {

  val actorSystem = ActorSystem("HelloAkka")
  println(actorSystem.name)
}
