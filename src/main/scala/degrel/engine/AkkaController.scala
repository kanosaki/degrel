package degrel.engine

import akka.actor.ActorSystem

class AkkaController(val name: String) {
  // From http://doc.akka.io/docs/akka/snapshot/java/dispatchers.html (2.3-SNAPSHOT)
  private var actorSystem: ActorSystem = null

  def boot() = {
    actorSystem = ActorSystem.create(name)
  }

  def shutdown() = {
    actorSystem match {
      case null => throw new IllegalStateException("You cannot shutdown not booted engine.")
      case _ => actorSystem.shutdown()
    }
  }

  def system = {
    actorSystem
  }
}
