package lectures

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, Promise}
import scala.util.Random

object ProducerConsumer extends App {

  val capacity = 100

  class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random()

      while (true) {
        buffer.synchronized {
          while (buffer.isEmpty) {
            println(s"[Consumer-$id] buffer empty, waiting ...")
            buffer.wait()
          }

          val x = buffer.dequeue()
          println(s"[Consumer-$id] consumed " + x)

          buffer.notify()
        }

        Thread.sleep(random.nextInt(200))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      var i = 0

      while (true) {
        buffer.synchronized {
          while (buffer.size == capacity) {
            println(s"[Producer-$id] buffer is full, waiting ...")
            buffer.wait()
          }

          println(s"[Producer-$id] producing " + i)
          buffer.enqueue(i)

          buffer.notify()

          i += 1
        }

        Thread.sleep(random.nextInt(200))
      }
    }
  }

  def multiProdCons(nConsumers: Int, nProducers: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]

    (1 to nConsumers).foreach(i => new Consumer(i, buffer).start())
    (1 to nProducers).foreach(i => new Producer(i, buffer).start())
  }

  multiProdCons(5, 5)
}
