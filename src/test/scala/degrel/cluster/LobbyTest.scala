package degrel.cluster

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import degrel.cluster.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class LobbyTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("LobbyTest", ConfigFactory.load("degrel").withFallback(ConfigFactory.load())))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Initial lobby" must {
    "is not ready" in {
      val lobby = system.actorOf(Props[Lobby])
      lobby ! QueryStatus()
      expectMsg(LobbyState(false))
    }

    "spawns session" in {
      val lobby = system.actorOf(Props[Lobby])
      lobby ! NewSession()
      val session = expectMsgPF() {
        case Right(sess: ActorRef) => sess
      }
      session ! QueryStatus()
      expectMsgPF() {
        case _: SessionState =>
      }
    }
  }

  "Lobby with a worker" must {
    "ready" in {
      val lobby = system.actorOf(Props[Lobby])
      val worker = system.actorOf(Worker.props(lobby.path.address, lobby))
      within(1.seconds) {
        awaitCond {
          lobby ! QueryStatus()
          expectMsgPF() {
            case LobbyState(true) => true
            case _ => false
          }
        }
      }
    }
  }
}
