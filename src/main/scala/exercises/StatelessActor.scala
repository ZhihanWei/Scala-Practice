package exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import exercises.StatefulActor.system
import exercises.StatelessActor.Citizen.{Vote, VoteStatusReply}
import exercises.StatelessActor.VoteAggregator.AggregateVotes

object StatelessActor extends App {
  val system = ActorSystem("ActorSystem")

  object StatelessCounter {

    case object Increment

    case object Decrement

    case object Print

  }

  class StatelessCounter extends Actor {

    import StatelessCounter._

    override def receive: Receive = countReceive(0)

    // To convert stateful to stateless, encapsulate the state with a wrapper
    def countReceive(currentCount: Int): Receive = {
      case Increment => context.become(countReceive(currentCount + 1))
      case Decrement => context.become(countReceive(currentCount - 1))
      case Print => println(s"[statelessCounter] my current count is $currentCount")
    }
  }

  val statelessCounter = system.actorOf(Props[StatelessCounter], "statelessCounter")
  (1 to 5).foreach { _ =>
    statelessCounter ! StatelessCounter.Increment
    statelessCounter ! StatelessCounter.Print
  }
  (1 to 4).foreach { _ =>
    statelessCounter ! StatelessCounter.Decrement
    statelessCounter ! StatelessCounter.Print
  }


  object Citizen {

    case object VoteStatusRequest

    case class Vote(candidate: String)

    case class VoteStatusReply(candidate: Option[String])

  }

  class Citizen extends Actor {

    import Citizen._

    override def receive: Receive = {
      case Vote(c) => context.become(voted(c))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  object VoteAggregator {

    case class AggregateVotes(citizen: Set[ActorRef])

  }

  class VoteAggregator extends Actor {

    import VoteAggregator._
    import Citizen._

    var stillWaiting: Set[ActorRef] = Set()
    var currentStats: Map[String, Int] = Map()

    override def receive: Receive = {
      case AggregateVotes(citizens) =>
        stillWaiting = citizens
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
      // may cause infinite loop here
      case VoteStatusReply(None) =>
        sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        currentStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty)
          println(s"[voteAggregator] poll status: $currentStats")
        else
          stillWaiting = newStillWaiting
    }
  }

  class StatelessVoteAggregator extends Actor {

    import VoteAggregator._
    import Citizen._

    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      // may cause infinite loop here
      case VoteStatusReply(None) =>
        sender() ! VoteStatusRequest

      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))

        if (newStillWaiting.isEmpty)
          println(s"[statelessVoteAggregator] poll status: $newStats")
        else
          context.become(awaitingStatuses(newStillWaiting, newStats))
    }
  }

  val citizenA = system.actorOf(Props[Citizen])
  val citizenB = system.actorOf(Props[Citizen])
  val citizenC = system.actorOf(Props[Citizen])
  val citizenD = system.actorOf(Props[Citizen])

  citizenA ! Vote("Bush")
  citizenB ! Vote("Obama")
  citizenC ! Vote("Obama")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(citizenA, citizenB, citizenC))

  val statelessVoteAggregator = system.actorOf(Props[StatelessVoteAggregator])
  statelessVoteAggregator ! AggregateVotes(Set(citizenA, citizenB, citizenC))
}
