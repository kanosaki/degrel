package degrel.engine

import degrel.cluster.LocalNode
import degrel.core.Vertex

/**
  * Driverを作成します．この段階では`Chassis`への登録は行われていません
  */
trait DriverFactory {

  protected def createLocalDriver(chassis: Chassis, cell: Vertex, node: LocalNode, parent: Driver): Driver = {
    if (parent == null) {
      new RootLocalDriver(cell, chassis, node)
    } else {
      new LocalDriver(cell, chassis, node, parent)
    }
  }

  protected def createDriver(chassis: Chassis, cell: Vertex, parent: Driver): Driver = {
    this.createLocalDriver(chassis, cell, LocalNode.current, parent)
  }

  def create(chassis: Chassis, cell: Vertex, parent: Driver): Driver = {
    val driver = this.createDriver(chassis, cell, parent)
    driver
  }
}

class BasicDriverFactory extends DriverFactory {
}

object DriverFactory {
  val default = new BasicDriverFactory()
}
