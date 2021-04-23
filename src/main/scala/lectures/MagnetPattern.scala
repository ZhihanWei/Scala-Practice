package lectures

import scala.concurrent.Future
import scala.jdk.CollectionConverters
import scala.concurrent.ExecutionContext.Implicits.global

object MagnetPattern extends App{

  class P2PRequest
  class P2PResponse
  class Serializer[T]

  trait Actor {
    def receive(statusCode: Int): Int
    def receive(request: P2PRequest): Int
    def receive(request: P2PResponse): Int
    def receive[T: Serializer](message: T): Int
    def receive[T: Serializer](message: T, statusCode: Int): Int
    def receive(future: Future[P2PRequest]): Int
  }

  /**
   * Problems:
   *
   * 1 - type erasure. E.g.
   *   def receive(future: Future[P2PResponse]): Int won't compile
   * 2 - lifting doesn't work for all overloads. E.g.
   *   val receiveFv = receive _
   * 3 - code duplication
   * 4 - type inference and default args. E.g.
   *   actor.receive()
   */

  trait MessageMagnet[Result] {
    def apply(): Result
  }

  def receive[R](magnet: MessageMagnet[R]): R = magnet()

  // 1 - no more type erasure problems
  implicit class FromP2PRequest(request: P2PRequest) extends MessageMagnet[Int] {
    def apply(): Int = 32
  }
  implicit class FromP2PResponse(response: P2PResponse) extends MessageMagnet[Int] {
    def apply(): Int = 3
  }

  println(receive(new P2PRequest))
  println(receive(new P2PResponse))

  // 2 - lifting works
  trait MathLib {
    def add1(x: Int): Int = x + 1
    def add1(s: String): Int = s.toInt + 1
  }

  trait AddMagnet {
    def apply(): Int
  }

  def add1(magnet: AddMagnet): Int = magnet()

  implicit class AddInt(x: Int) extends AddMagnet {
    override def apply(): Int = x + 1
  }
  implicit class AddString(s: String) extends AddMagnet {
    override def apply(): Int = s.toInt + 1
  }

  val addFV = add1 _
  println(addFV(1))
  println(addFV("3"))

  // Implicit class doesn't work for pass by name
}
