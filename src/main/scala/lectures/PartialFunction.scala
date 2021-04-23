package lectures

object PartialFunction extends App {
  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 101
    case 5 => 105
    case 23 => 123
  }

  println(aPartialFunction(1))
  println(aPartialFunction.lift(10))
}
