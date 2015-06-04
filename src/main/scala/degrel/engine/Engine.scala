package degrel.engine

import akka.actor.ActorSystem

class Engine(val chassis: Chassis, val name: String) {

  val akka = new AkkaController(name)

  def boot(): Unit = akka.boot()

  def system: ActorSystem = akka.system
}

object Engine {
  val DEFAULT_ENGINE_NAME = "degrel"
  def apply() = {
    new Engine(Chassis.createWithMain(), DEFAULT_ENGINE_NAME)
  }
}

