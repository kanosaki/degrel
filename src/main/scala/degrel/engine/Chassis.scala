package degrel.engine

import degrel.core.{Cell, Label}
import degrel.engine.namespace.Repository
import degrel.engine.sphere.Sphere

/**
 * 名前空間を管理します
 * @todo 冗長?
 * @param _repo 管理する名前空間
 */
class Chassis(_repo: Repository) {
  var sphere: Sphere = degrel.engine.sphere.default
  var verbose = false

  def getResourceFor(driver: Driver): Sphere = sphere

  def getDriver(name: String): Option[Driver] = {
    val key = name.split(namespace.NAME_DELIMITER).map(Symbol(_)).toList
    this.getDriver(key)
  }

  def getDriver(name: List[Symbol]): Option[Driver] = {
    this.repository.get(name)
  }

  def repository: namespace.Repository = _repo

  def getName(drv: Driver): String = {
    this.repository.getName(drv).map(_.name).mkString(namespace.NAME_DELIMITER)
  }

  def main: Driver = {
    repository.get(Label.N.main).get
  }
}

object Chassis {
  def create(): Chassis = {
    this.create(Cell())
  }

  def create(main: Cell): Chassis = {
    val repo = new Repository()
    val chassis = new Chassis(repo)
    repo.register(Label.N.main, new Driver(main, chassis))
    chassis
  }

  def createWithMain(initRepo: Repository = null): Chassis = {
    val ch = Chassis.create(initRepo)
    ch.repository.register(Label.N.main, new Driver(Cell(Seq()), ch))
    ch
  }

  def create(initRepo: Repository): Chassis = {
    val repo = if (initRepo == null) {
      new Repository()
    } else {
      initRepo
    }
    new Chassis(repo)
  }
}
