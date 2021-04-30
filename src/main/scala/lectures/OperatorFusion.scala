package lectures

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source, Flow}

object OperatorFusion extends App {
  implicit val system = ActorSystem("OperatorFusionStreams")

  val simpleSource = Source(1 to 10)

  val complexFlow1 = Flow[Int].map { x =>
    Thread.sleep(1000)
    x + 1
  }

  val complexFlow2 = Flow[Int].map { x =>
    Thread.sleep(1000)
    x * 10
  }

  val simpleSink = Sink.foreach(println)

  // async boundary with ordering guarantees
  simpleSource.via(complexFlow1).async
    .via(complexFlow2).async
    .to(simpleSink)
    .run()


}
