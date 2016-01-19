package degrel.cluster

import akka.actor.ActorRef
import akka.cluster.Member
import akka.pattern.ask

import scala.collection.mutable
import scala.concurrent.Future


/**
  * Host(Ocean, Island, Controller)とSessionを管理します
  * Controllerからの依頼を受けて，適切なIslandからNodeを割り当てたりします
  * また，リソースの管理等も行います
  * TODO: 複数のOceanがいたときに，協調して動作するように
  */
class Lobby extends MemberBase {
  // mapping: controller -> SessionManager
  val sessions = mutable.HashMap[ActorRef, ActorRef]()

  // mapping: Worker -> SessionManager
  val allocationMapping = mutable.HashMap[ActorRef, ActorRef]()

  import context.dispatcher
  import messages._

  private def allocateNode(manager: ActorRef, param: NodeInitializeParam): Future[Option[ActorRef]] = {
    implicit val timeout = Timeouts.short
    workers.find(w => !allocationMapping.contains(w)) match {
      case Some(worker) => {
        (worker ? SpawnNode(manager, param)) map {
          case Right(node: ActorRef) => {
            allocationMapping += worker -> manager
            Some(node)
          }
        }
      }
      case None => {
        Future {
          None
        }
      }
    }
  }

  private def allocateSession(ctrlr: ActorRef): ActorRef = {
    val session = context.actorOf(SessionManager.props(self, ctrlr))
    sessions += ctrlr -> session
    session
  }


  override def onUnregistered(member: Member): Unit = {
    if (member.hasRole(Roles.Controller.name)) {
      val deadSessions = sessions.filter {
        case (ctrlr, session) => ctrlr.path.address == member.address
      }.valuesIterator
      deadSessions.foreach(this.releaseSession)
    }
    if (member.hasRole(Roles.Worker.name)) {
      val deadWorkers = allocationMapping.filter {
        case (session, worker) => worker.path.address == member.address
      }.valuesIterator
      deadWorkers.foreach { w =>
        log.debug(s"Freeing worker $w")
        allocationMapping -= w
      }
    }
  }

  def releaseSession(sess: ActorRef) = {
    log.info(s"Closing session $sess")
    sessions -= sess
    allocationMapping.filter {
      case (worker, session) => {
        println(s"CHECKING NODE: $worker $session $sess")
        session == sess
      }
    }.keysIterator.foreach { w =>
      log.debug(s"Freeing worker $w")
      allocationMapping -= w
    }
  }

  override def receiveMsg: Receive = {
    case QueryStatus() => {
      sender() ! LobbyState(active = this.workers.nonEmpty)
    }
    case NewSession(ctrlr) => {
      val newSession = allocateSession(ctrlr)
      log.info(s"New Session: $newSession")
      sender() ! Right(newSession)
    }
    case CloseSession(sess) => {
      this.releaseSession(sess)
      context.stop(sess)
    }
    case NodeAllocateRequest(manager, param) => {
      log.info(s"Allocating node for: $manager param: $param")
      val origin = sender()
      allocateNode(manager, param).onSuccess {
        case Some(node) => {
          origin ! Right(node)
        }
        case None => {
          origin ! Left(new RuntimeException("No free worker!"))
        }
      }
    }
  }

  override def role: MemberRole = Roles.Lobby
}
