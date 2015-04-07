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

  def getDriver(name: String): Option[Driver] = {
    val key = name.split(namespace.NAME_DELIMITER).map(Symbol(_)).toList
    this.getDriver(key)
  }

  def getDriver(name: List[Symbol]): Option[Driver] = {
    this.repository.get(name)
  }

  def getName(drv: Driver): String = {
    this.repository.getName(drv).map(_.name).mkString(namespace.NAME_DELIMITER)
  }

  def main: Driver = {
    repository.get(Label.N.main).get
  }
}

object Chassis {
  def createMain(name: String): Chassis = {
    val ch = new Chassis(name)
    ch.repository.register(Label.N.main, new Driver(Cell(Seq())))
    ch
  }
}
