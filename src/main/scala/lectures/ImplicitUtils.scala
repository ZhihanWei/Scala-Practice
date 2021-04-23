package lectures

object ImplicitUtils extends App {
  /**
   * Implicit scope
   * 1) local scope
   * 2) imported scope
   * 3) companions of all types involved in the method signature
   *    - List
   *    - Ordering
   *    - all the types involved = A or any supertype
   *    e.g. def sorted[B >: A](implicit ord: Ordering[B]): List[B]
   */

  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  object HTMLSerializer{
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String =
      serializer.serialize(value)

    def apply[T](implicit serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
  }

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<dev style: color=blue>$value</dev>"
  }

  println(HTMLSerializer.serialize(42))
  println(HTMLSerializer[Int].serialize(42))

  implicit class HTMLEnrichment[T](value: T) {
    def toHTML(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }

  println(32.toHTML)

  /**
   * Implicit class must be defined inside of `trait`/`class`/`object`
   * [[https://docs.scala-lang.org/overviews/core/implicit-classes.html]]
   */
  implicit class RichString(string: String) {
    def asInt: Int = Integer.valueOf(string)
    def encrypt(cypherDistance: Int): String = string.map(c => (c +  cypherDistance).asInstanceOf[Char])
  }

  println("3".asInt + 5)
  println("John" encrypt 2)
 }
