package lectures

/**
 * Test `object` with `unapply` for pattern matching,
 * Can also try `case object` for pattern matching.
 */
object PatternMatching extends App {

  object EvenNumber {
    def unapply(number: Int): Boolean = number % 2 == 0
  }

  object SingleDigit {
    def unapply(number: Int): Boolean = -10 < number && number < 10
  }

  val theNumber: Int = 46
  val mathProperty = theNumber match {
    case SingleDigit() => "Single digit"
    case EvenNumber() => "An even number"
    case _ => "No property"
  }

  println(mathProperty)
}
