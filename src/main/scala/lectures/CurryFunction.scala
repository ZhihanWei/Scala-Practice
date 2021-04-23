package lectures

object CurryFunction extends App {
  val superAdder: Int => Int => Int =
    x => y => x + y

  val add3 = superAdder(3)
  println(add3(5))
  //curry function
  println(superAdder(3)(5))

  // curried method
  def curriedAdder(x: Int)(y: Int): Int = x + y

  val add4: Int => Int = curriedAdder(4)
  val add5 = curriedAdder(5) _ // ETA-expansion
  println(add4(4))
  println(add5(3))

  // different ways of define function / method
  val simpleAddFunction = (x: Int, y: Int) => x + y

  def simpleAddMethod(x: Int, y: Int) = x + y

  def curriedAddMethod(x: Int)(y: Int) = x + y

  val add7_1 = (x: Int) => simpleAddFunction(7, x)
  val add7_2 = simpleAddFunction.curried(7)
  // ETA-expansion
  val add7_3 = curriedAddMethod(7) _
  val add7_4 = curriedAddMethod(7)(_)
  val add7_5 = simpleAddFunction(7, _)
  val add7_6 = simpleAddMethod(7, _)

  /**
   * EXERCISE
   *  - function vs method
   *  - parameters: by-name vs 0-lambda
   */
  def byName(n: Int) = n + 1
  def byFunction(f: () => Int) = f() + 1

  // parameterless method directly replace method with value
  def method: Int = 42
  // method with parentheses
  def parenMethod(): Int = 42

  /**
   * Calling byName and byFunction
   *  - int
   *  - method
   *  - parenMethod
   *  - lambda
   *  - partial function
   */
  byName(23)
  byName(method)
  byName(parenMethod())
  byName((()=> 23)())

  byFunction(() => 23)
}
