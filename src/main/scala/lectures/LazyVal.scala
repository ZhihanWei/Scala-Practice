package lectures

import scala.util.Try

object LazyVal extends App {
  def retrieveValue(): Int = {
    println("Waiting")
    Thread.sleep(1000)
    23
  }

  // call method by feeding parameter value
  def evaluateOnceByValue(n: Int): Int = {
    n + n + n + 1
  }

  // call method by feeding parameter name
  def evaluateByName(n: => Int): Int = {
    // call by need
    lazy val t = n
    t + t + t + 1
  }

  def evaluateMultipleTimes(n: => Int): Int = {
    n + n + n + 1
  }

  println("Evaluate three times")
  println(evaluateMultipleTimes(retrieveValue()))

  println("Evaluate once with lazy val")
  println(evaluateByName(retrieveValue()))

  println("Evaluate once with function result as  parameter")
  println(evaluateByName(retrieveValue()))
  println

  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }

  def greaterThan20(i: Int): Boolean = {
    println(s"$i is greater than 20?")
    i > 20
  }

  val numbers = List(1, 2, 30, 23, 49, 8, 891)

  println("Regular evaluate")
  val lt30 = numbers.filter(lessThan30)
  val gt20 = lt30.filter(greaterThan20)
  println(gt20)

  println
  println("Lazy evaluate")
  val lt30Lazy = numbers.withFilter(lessThan30)
  val gt20Lazy = lt30Lazy.withFilter(greaterThan20)
  println(gt20Lazy)
  gt20Lazy.foreach(println)

  // These two are equivalent
  for {
    e <- numbers if e % 2 == 0
  } yield e + 1

  numbers.withFilter(_ % 2 == 0).map(_ + 1)
}
