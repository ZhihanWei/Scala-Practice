package lectures

object SelfType extends App {
  trait Instrumentalist {
    def play(): Unit
  }

  // (Self Type) whoever implements Singer should implement Instrumentalist
  trait Singer { self: Instrumentalist =>
    def sing(): Unit
  }

  class LeadSinger extends Singer with Instrumentalist {
    override def play(): Unit = ???
    override def sing(): Unit = ???
  }

  val jamsHetField = new Singer with Instrumentalist {
    override def play(): Unit = ???
    override def sing(): Unit = ???
  }

  // self type VS inheritance
  // self type is check at compile time, inheritance checked at runtime
  class A
  class B extends A // B is an A

  trait T
  trait S { self: T => } // S requires a T

  // Cake Pattern
  trait ScalaComponent {
    def action(x: Int): String
  }
  trait ScalaDependentComponent { self: ScalaComponent =>
    def dependentAction(x: Int): String = action(x) + " the rock!"
  }
  trait ScalaApplication { self: ScalaDependentComponent => }
  // layer 1
  trait Picture extends ScalaComponent
  trait Stats extends ScalaComponent
  // layer 2
  trait Profile extends ScalaDependentComponent with Picture
  trait Analytics extends ScalaDependentComponent with Stats
  // layer 3
  trait AnalyticsApp extends ScalaApplication with Analytics

  // cyclical dependencies, not possible for inheritance but possible for self type
  trait X { self: Y => }
  trait Y { self: X => }

}
