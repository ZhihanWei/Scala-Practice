package exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object WordCount extends App {
  val system = ActorSystem("ActorSystem")

  val nWorker = 8
  val workerSleepMs = 1000
  val nExperiment = 100

  object WordCounterMaster {

    case class Initialize(nChildren: Int)

    case class WordCountTask(masterName: String, text: String)

    case class WordCountReply(count: Int)
  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._
    import WordCounterWorker._

    override def receive: Receive = init

    def init: Receive = {
      case Initialize(nWorker) =>
        (0 until nWorker).foreach{id =>
          println(s"worker $id has been started")
          context.actorOf(Props[WordCounterWorker], s"worker-$id")
        }
        context.become(jobDispatch(0, nWorker))
    }

    def jobDispatch(workerId: Int, nWorker: Int): Receive = {
      case WordCountTask(masterName, text) =>
        val workerName = s"worker-$workerId"
        context.actorSelection(s"/user/$masterName/$workerName") ! countWord(workerId, text)
        context.become(jobDispatch((workerId + 1) % nWorker, nWorker))
      case WordCountReply(count) =>
          if (count != nExperiment) println(s"[master] Worker Count: $count")
    }

  }


  object WordCounterWorker {
    case class countWord(id: Int, word: String)
  }

  class WordCounterWorker extends Actor {

    import WordCounterMaster._
    import WordCounterWorker._

    override def receive: Receive = {
      case countWord(id, word) =>
        Thread.sleep(workerSleepMs)
        println(s"[worker-$id] start counting")
        val nWord = word.split(" ").length
        sender() ! WordCountReply(nWord)
    }
  }

  import WordCounterMaster._

  val masterName = "master"
  val masterRef = system.actorOf(Props[WordCounterMaster], masterName)

  masterRef ! Initialize(nWorker)

  def timedFuture[T](future: Future[T]): Unit = {
    val start = System.currentTimeMillis()
    future.onComplete(_ => println(s"Future took ${System.currentTimeMillis() - start} ms"))
  }

  val parallelCounting = Future.sequence(
    (1 to nExperiment).map { nWord =>
    Future {
      val word = List.fill(nWord)("word").mkString(" ")
      masterRef ! WordCountTask(masterName, word)
    }
  }).map(_ => ())

  timedFuture(parallelCounting)
}