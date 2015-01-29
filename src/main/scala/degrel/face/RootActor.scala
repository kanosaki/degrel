package degrel.face

import akka.actor.{Actor, ActorRefFactory}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

class RootActor(implicit val inj: Injector) extends Actor with RootRouter with AkkaInjectable {

  def actorRefFactory: ActorRefFactory = context

  def receive = runRoute(root)
}
