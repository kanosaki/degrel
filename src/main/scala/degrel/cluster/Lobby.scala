package degrel.cluster

import akka.actor.ActorRef

import scala.collection.mutable


/**
  * Host(Ocean, Island, Controller)とSessionを管理します
  * Controllerからの依頼を受けて，適切なIslandからNodeを割り当てたりします
  * また，リソースの管理等も行います
  * TODO: 複数のOceanがいたときに，協調して動作するように
  */
class Lobby extends MemberBase {

  val wks = mutable.ListBuffer[ActorRef]()

  import messages._

  override def receiveMsg: Receive = {
    case WorkerRegistration(workerRef) => {
      println(s"WORKER REGISTER: $workerRef")
      this.wks += workerRef
    }
    case NewSession() =>
    case NodeAllocateRequest() =>
  }
}
