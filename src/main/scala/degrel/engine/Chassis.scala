package degrel.engine

import degrel.cluster.LocalNode
import degrel.core.{Cell, Label, Vertex}
import degrel.engine.namespace.Repository
import degrel.engine.sphere.Sphere
import org.json4s.JsonAST.JObject

import org.json4s.JsonDSL._

/**
  * 名前空間を管理し`Driver`のためのfactoryクラスとして動作します
  * @todo 冗長?
  * @param _repo 管理する名前空間(Local or Remote)
  */
class Chassis(_repo: Repository, var driverFactory: DriverFactory = DriverFactory.default) {
  var sphere: Sphere = degrel.engine.sphere.default
  var verbose = false

  def getResourceFor(driver: LocalDriver): Sphere = sphere

  def normalizeName(name: String): List[Symbol] = {
    name.split(namespace.NAME_DELIMITER).map(Symbol(_)).toList
  }

  def getDriver(name: String): Option[LocalDriver] = {
    val key = this.normalizeName(name)
    this.getDriver(key)
  }

  def getDriver(name: List[Symbol]): Option[LocalDriver] = {
    this.repository.get(name)
  }

  def createDriver(cell: Vertex, parent: LocalDriver = null): LocalDriver = {
    driverFactory.create(this, cell, parent)
  }

  def addDriver(name: List[Symbol], driver: LocalDriver): LocalDriver = {
    this.repository.register(name, driver)
    driver
  }

  def registerCell(name: List[Symbol], cell: Vertex, parent: LocalDriver = null): LocalDriver = {
    val driver = this.driverFactory.create(this, cell, parent)
    this.addDriver(name, driver)
  }

  def register(name: String, cell: Vertex, parent: LocalDriver = null): LocalDriver = {
    val normalizedName = this.normalizeName(name)
    this.registerCell(this.normalizeName(name), cell: Vertex, parent: LocalDriver)
  }

  def repository: namespace.Repository = _repo

  def getName(drv: LocalDriver): String = {
    this.repository.getName(drv).map(_.name).mkString(namespace.NAME_DELIMITER)
  }

  def main: LocalDriver = {
    repository.get(Label.N.main).get
  }

  object diagnostics {
    var rewriteTryCount: Long = 0
    var rewriteExecCount: Long = 0
    val rewriteSpan = new ProcedureSpan("rewrite")
    val matchSpan = new ProcedureSpan("match")
    val buildSpan = new ProcedureSpan("build")
    val applySpan = new ProcedureSpan("apply")
    val spawnSpan = new ProcedureSpan("spawn")
    val fingerprintCheckSpan = new ProcedureSpan("fingerprintCheck")

    def spans = Seq(rewriteSpan, matchSpan, buildSpan, applySpan, spawnSpan, fingerprintCheckSpan)
  }

}

class ProcedureSpan(val name: String) {
  var accNanoTime: Long = 0
  var callCount: Long = 0

  def aveNanoTime: Long = {
    accNanoTime / callCount
  }

  def enter[T](f: => T): T = {
    val begin = System.nanoTime()
    val ret = f
    val duration = System.nanoTime() - begin
    callCount += 1
    accNanoTime += duration
    ret
  }

  def toJson: JObject = {
    ("name" -> name) ~
      ("accNanoTime" -> accNanoTime) ~
      ("callCount" -> callCount)
  }
}

object Chassis {
  def apply(repo: Repository, factory: DriverFactory): Chassis = {
    new Chassis(repo, factory)
  }

  def create(): Chassis = {
    this.create(Cell())
  }

  def create(main: Cell): Chassis = {
    val repo = new Repository()
    val chassis = new Chassis(repo)
    repo.register(Label.N.main, new LocalDriver(main, chassis, LocalNode.current))
    chassis
  }

  def createWithMain(initRepo: Repository = null): Chassis = {
    val ch = Chassis.create(initRepo)
    ch.repository.register(Label.N.main, new LocalDriver(Cell(Seq()), ch, LocalNode.current))
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
