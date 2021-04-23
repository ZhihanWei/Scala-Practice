package lectures

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object Dispatchers extends App {

  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] $message")
    }
  }

  val system = ActorSystem("DispatchersDemo")

  // Method - 1
//  val actors = for (i <- 1 to 3) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
//  val r = new Random()
//  (1 to 100) foreach { i =>
//    actors(r.nextInt(3)) ! i
//  }

  // Method - 2
//  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")

  class DBActor extends Actor with ActorLogging {
    implicit val executionContext: ExecutionContext = context.dispatcher

    override def receive: Receive = {
      case message => Future {
        Thread.sleep(5000)
        log.info(s"Success: $message")
      }
    }

    val dbActor: ActorRef = system.actorOf(Props[DBActor])
    dbActor ! "The meaning of life"
  }





}
