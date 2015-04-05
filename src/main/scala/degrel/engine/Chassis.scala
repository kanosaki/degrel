package degrel.engine

import akka.actor.ActorSystem
import degrel.core.{Label, Cell}
import degrel.engine.namespace.Repository

class Chassis(val name: String) {
  private val _repo = new Repository()

  val akka = new AkkaController(name)

  def boot(): Unit = akka.boot()

  def system: ActorSystem = akka.system

  def repository: namespace.Repository = _repo

  def main: Cell = {
    repository.get(Label.N.main).get
  }
}
