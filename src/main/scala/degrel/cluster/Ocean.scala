package degrel.cluster

import akka.actor.{ActorLogging, Actor}

/**
  * Host(Ocean, Island, Controller)とSessionを管理します
  * Controllerからの依頼を受けて，適切なIslandからNodeを割り当てたりします
  * TODO: 複数のOceanがいたときに，協調して動作するように
  */
class Ocean extends Actor with ActorLogging {
  override def receive: Receive = {
    ???
  }
}
