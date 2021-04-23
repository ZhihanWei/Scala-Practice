package lectures

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Timers}

import scala.concurrent.duration.DurationInt

object Timer extends App {

  val system = ActorSystem("TimerSystem")

  object TimerActor {
    case object TimerKey
    case object Start
    case object Reminder
    case object Stop
  }

  class TimerActor extends Actor with ActorLogging with Timers {
    import TimerActor._

    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startTimerWithFixedDelay(TimerKey, Reminder, 1 second)
      case Reminder =>
        log.info("I am alive")
      case Stop =>
        log.warning("Stopping!")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  import TimerActor._

  implicit val contextExecution = system.dispatcher

  val timerHeartbeatActor = system.actorOf(Props[TimerActor], "timerActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timerHeartbeatActor ! Stop
  }
}