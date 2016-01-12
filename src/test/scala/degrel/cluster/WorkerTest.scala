package degrel.cluster

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class WorkerTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("WorkerTest"))

  import messages._

  "Worker" must {
    "Spawns Session Node" in {
      val lobby = system.actorOf(Props[Lobby])
      val worker = system.actorOf(Worker.props(lobby.path.address, lobby))
      lobby ! NewSession()
      var session: ActorRef = null
      within(1.seconds) {
        awaitCond {
          session = expectMsgPF() {
            case Right(sess: ActorRef) => sess
            case _ => null
          }
          session != null
        }
      }
      worker ! SpawnNode(session, NodeInitializeParam(0))
      val node = expectMsgPF() {
        case Right(ref: ActorRef) => ref
      }
      within(1.seconds) {
        awaitCond {
          node ! QueryStatus()
          expectMsgPF() {
            case NodeState(_, _) => true
            case _ => false
          }
        }
      }
    }
  }
}
