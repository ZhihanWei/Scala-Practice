package lectures

import java.util.concurrent.atomic.AtomicReference
import scala.math.pow
import scala.collection.parallel.CollectionConverters._

object ParallelUtils extends App {
  def measure[T](operation: => T): Long = {
    val time = System.currentTimeMillis()
    operation
    System.currentTimeMillis() - time
  }

  /**
   * Collections can do parallel computing:
   * Seq, Vector, Array, Map-(Hash, Trie), Set-(Hash, Trie)
   */
  val list: Seq[Int] = (1 to pow(10, 8).toInt).toList
  val serialTime: Long = measure {
    list.map(_ + 1)
  }
  val parallelTime: Long = measure {
    list.par.map(_ + 1)
  }

  println("Serial time: " + serialTime)
  println("Parallel time: " + parallelTime)

  /**
   * Operation:
   * map, flatMap, filter, foreach, reduce, fold
   * might give different result if use parallel collections
   */
  var sum = 0
  // potential race condition
  List(1, 2, 3, 4, 5).par.foreach(sum += _)

  // Atomic

  val atomic = new AtomicReference[Int](2)
  val currentValue = atomic.get()
  atomic.set(4)
  atomic.getAndSet(5)
  atomic.compareAndSet(28, 56)
  atomic.updateAndGet(_ + 1)

}
