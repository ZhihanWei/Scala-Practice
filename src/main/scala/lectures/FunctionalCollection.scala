package lectures

object FunctionalCollection extends App {
  val set = Set(1, 2, 3)
  println(set(1))

  trait MySet[A] extends PartialFunction[A, Boolean] {

  }

  trait MyOtherSet[A] extends (A => Boolean) {

  }

}
