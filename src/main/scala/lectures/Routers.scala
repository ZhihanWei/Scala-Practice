package lectures

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, Broadcast, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router}

object Routers extends App {

  // Method - 1
  class Master extends Actor {
    // Step 1 - create routees
    // 5 actor routees based off Slave actors
    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    // Step 2 - define router
    /**
     * round-robin
     * random
     * smallest mailbox
     * broadcast
     * scatter-gather-first
     * tail-chopping
     * consistent-hashing
     */
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      // Step 3 - route the messages
      case message: String =>
        router.route(message, sender())
      // Step 4 - hendle the termination / lifecycle of the routees
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router.addRoutee(newSlave)
    }
  }


  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RoutersDemo")
  val master = system.actorOf(Props[Master])

//  for (i <- 1 to 10) {
//    master ! s"[$i] Hello from the world"
//  }

  // Method - 2
  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")
//  for (i <- 1 to 10) {
//    poolMaster ! s"[$i] Hello from the world"
//  }

  // Method - 3
//  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")
//  for (i <- 1 to 10) {
//    poolMaster2 ! s"[$i] Hello from the world"
//  }

  // Method - 4
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"slave_$i")).toList
  val slavePaths = slaveList map (slaveRef => slaveRef.path.toString)
  val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())
  for (i <- 1 to 10) {
    groupMaster ! s"[$i] Hello from the world"
  }

  groupMaster ! Broadcast("Hello, everyone")
}
