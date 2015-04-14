package degrel.engine

import degrel.core.{Cell, Label}
import degrel.engine.namespace.Repository

class Chassis(_repo: Repository) {

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

  def repository: namespace.Repository = _repo
}

object Chassis {
  def create(initRepo: Repository = null): Chassis = {
    val repo = if (initRepo == null) {
      new Repository()
    } else {
      initRepo
    }
    new Chassis(repo)
  }

  def createWithMain(initRepo: Repository = null): Chassis = {
    val ch = Chassis.create(initRepo)
    ch.repository.register(Label.N.main, new Driver(Cell(Seq())))
    ch
  }
}
