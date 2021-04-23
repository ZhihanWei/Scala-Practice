package exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object WordCountSolution extends App {
  val system = ActorSystem("ActorSystem")

  val nWorker = 8
  val workerSleepMs = 0
  val nExperiment = 100

  object WordCounterMaster {

    case class Initialize(nChildren: Int)

    case class WordCountTask(id: Int, text: String)

    case class WordCountReply(id: Int, count: Int)

  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    override def receive: Receive = init

    def init: Receive = {
      case Initialize(nWorker) =>
        println("[master] Initializing ......")
        val childrenRefs = (1 to nWorker).map { id =>
          println(s"[master] Initilized worker-$id")
          context.actorOf(Props[WordCounterWorker], s"worker-$id")
        }
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] I have received $text, I will send it to worker-$currentChildIndex")
        val originalSender = sender()
        val childRef = childrenRefs(currentChildIndex)
        childRef ! WordCountTask(currentTaskId, text)
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        println(s"[master] I have received a reply for task id $id with $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        Thread.sleep(workerSleepMs)
        println(s"[worker] ${self.path} I have received task $id with $text")
        val nWord = text.split(" ").length
        sender() ! WordCountReply(id, nWord)
    }
  }

  class TestActor extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("a b c", "a b c d e", "yes", "pop eye")
        texts.foreach(text => master ! text)
      case count: Int =>
        println(s"[TestActor] I received a reply: $count")
    }
  }

  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"
}
