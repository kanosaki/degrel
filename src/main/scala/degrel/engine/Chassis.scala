package degrel.engine

import degrel.core.{Cell, Label, Vertex}
import degrel.engine.namespace.Repository
import degrel.engine.sphere.Sphere

/**
 * 名前空間を管理し`Driver`のためのfactoryクラスとして動作します
 * @todo 冗長?
 * @param _repo 管理する名前空間
 */
class Chassis(_repo: Repository, var driverFactory: DriverFactory = DriverFactory.default) {
  var sphere: Sphere = degrel.engine.sphere.default
  var verbose = false

  def getResourceFor(driver: Driver): Sphere = sphere

  def normalizeName(name: String): List[Symbol] = {
    name.split(namespace.NAME_DELIMITER).map(Symbol(_)).toList
  }

  def getDriver(name: String): Option[Driver] = {
    val key = this.normalizeName(name)
    this.getDriver(key)
  }

  def getDriver(name: List[Symbol]): Option[Driver] = {
    this.repository.get(name)
  }

  def createDriver(cell: Vertex, name: List[Symbol], parent: Driver = null): Driver = {
    new Driver(cell, this)
  }

  def addDriver(name: List[Symbol], driver: Driver): Driver = {
    this.repository.register(name, driver)
    driver
  }

  def registerCell(name: List[Symbol], cell: Vertex, parent: Driver = null): Driver = {
    val driver = this.driverFactory.create(this, cell, parent)
    this.addDriver(name, driver)
  }

  def register(name: String, cell: Vertex, parent: Driver = null): Driver = {
    val normalizedName = this.normalizeName(name)
    this.registerCell(this.normalizeName(name), cell: Vertex, parent: Driver)
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
