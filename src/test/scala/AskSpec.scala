import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class AskSpec extends TestKit(ActorSystem("AskSpec")) with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import AskSpec._

  def authenticatorTestSuite(props: Props): Unit = {
    import AuthManager._

    "fail to authenticate a non-registered user" in {
      val authManager = system.actorOf(props)
      authManager ! Authenticate("abc", "password")
      expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
    }

    "fail to authenticate if invalid password" in {
      val authManager = system.actorOf(props)
      authManager ! RegisterUser("abc", "password")
      authManager ! Authenticate("abc", "passwordAgain")
      expectMsg(AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT))
    }

    "successfully authenticate a registered user" in {
      val authManager = system.actorOf(props)
      authManager ! RegisterUser("abc", "password")
      authManager ! Authenticate("abc", "password")
      expectMsg(AuthSuccess)
    }
  }

  "An authenticator" should {
    authenticatorTestSuite(Props[AuthManager])
  }

  "A piped authenticator" should {
    authenticatorTestSuite(Props[PipedAuthManager])
  }
}

object AskSpec {

  case class Read(key: String)
  case class Write(key: String, value: String)

  class KVActor extends Actor with ActorLogging {
    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Trying to read the value at the key $key")
        sender() ! kv.get(key)
      case Write(key, value) =>
        log.info(s"Writing the value $value for the key $key")
        context.become(online(kv + (key -> value)))
    }
  }

  case class RegisterUser(username: String, password: String)
  case class Authenticate(username: String, password: String)
  case class AuthFailure(message: String)
  case object AuthSuccess
  object AuthManager {
    val AUTH_FAILURE_NOT_FOUND = "username not found"
    val AUTH_FAILURE_PASSWORD_INCORRECT = "password incorrect"
    val AUTH_FAILURE_SYSTEM = "system error"
  }

  class AuthManager extends Actor with ActorLogging {
    import AuthManager._

    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    protected val authDb: ActorRef = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) => authDb ! Write(username, password)
      case Authenticate(username, password) => handleAuthentication(username, password)
    }

    def handleAuthentication(username: String, password: String): Unit = {
      val originalSender = sender()
      val future = authDb ? Read(username)

      // NEVER CALL METHOD IN THE ACTOR INSTANCE OR ACCESS MUTABLE STATE IN ONCOMPLETE
      // AVOID CLOSING OVER THE ACTOR INSTANCE OR MUTABLE STATE
      // Don't use `sender()` in the onComplete directly, since it will execute on the other thread, and the sender() may not be the original sender
      future.onComplete {
        case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Success(Some(dbPassword)) =>
          if (dbPassword == password) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
        case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
      }
    }
  }

  class PipedAuthManager extends AuthManager {
    import AuthManager._

    override def handleAuthentication(username: String, password: String): Unit = {
      val future = authDb ? Read(username) // Future[Any]
      val passwordFuture = future.mapTo[Option[String]]
      val responseFuture = passwordFuture.map {
        case Some(dbPassword) =>
          if (dbPassword == password) AuthSuccess
          else AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
        case None => AuthFailure(AUTH_FAILURE_NOT_FOUND)
      }

      // when the future completes, send the response to the actor ref in the argument list
      responseFuture.pipeTo(sender())
    }
  }

}