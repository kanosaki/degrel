package degrel.engine

import degrel.cluster.LocalNode
import degrel.core.Vertex

/**
  * Driverを作成します．この段階では`Chassis`への登録は行われていません
  */
trait DriverFactory {
  def configurators: Seq[Driver => Unit] = Seq()

  protected def createDriver(chassis: Chassis, cell: Vertex, parent: Driver): Driver = {
    if (parent == null) {
      new RootLocalDriver(cell, chassis, LocalNode.current)
    } else {
      new LocalDriver(cell, chassis, LocalNode.current, parent)
    }
  }

  protected def configureDriver(driver: Driver): Unit = {
    configurators.foreach(_ (driver))
  }

  def create(chassis: Chassis, cell: Vertex, parent: Driver): Driver = {
    val driver = this.createDriver(chassis, cell, parent)
    this.configureDriver(driver)
    driver
  }
}

class BasicDriverFactory extends DriverFactory {
}

object DriverFactory {
  val default = new BasicDriverFactory()
}
