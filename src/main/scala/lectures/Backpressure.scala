package lectures

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

object Backpressure extends App {

  implicit val system = ActorSystem("Backpressure")

  val fastSource = Source(1 to 50)
  val slowSink = Sink.foreach[Int] { x =>
    Thread.sleep(1000)
    println(s"Sink: $x")
  }

  // this is not backpressure because it's all run on one actor which is single thread
//  fastSource.to(slowSink).run()

  // this is backpressure
//  fastSource.async.to(slowSink).run()

  val simpleFlow = Flow[Int].map { x =>
    println(s"Incoming: $x")
    x + 1
  }

  // backpressure in action, the default buffer is 16
//  fastSource.async.via(simpleFlow).async.to(slowSink).run()

  /**
   * the component react to backpressure in the following ways:
   *  - try to slow down if possible
   *  - buffer elements until more demand
   *  - drop down elements from the buffer if it overflows
   *  - tear down/kill the whole stream (aka. failure)
   */
  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
//  fastSource.async.via(bufferedFlow).async.to(slowSink).run()

  /**
   * overflow strategies:
   *  - drop head = oldest
   *  - drop tail = newest
   *  - drop new = exact element to be added = keeps the current buffer
   *  - drop the entire buffer
   *  - backpressure signal
   *  - fali
   */

  // throttling, control how fast the source emit new element
  import scala.concurrent.duration._
  fastSource.throttle(10, 1 second).runWith(Sink.foreach(println))
}


