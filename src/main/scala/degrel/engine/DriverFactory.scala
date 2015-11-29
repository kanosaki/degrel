package degrel.engine

import com.typesafe.config.ConfigFactory
import degrel.cluster.LocalNode
import degrel.core.{VertexHeader, Vertex}

/**
  * Driverを作成します．この段階では`Chassis`への登録は行われていません
  */
trait DriverFactory {
  protected def createDriver(chassis: Chassis, cell: VertexHeader, parent: Driver): Driver

  protected def createLocalDriver(chassis: Chassis, cell: VertexHeader, node: LocalNode, parent: Driver): LocalDriver = {
    if (parent == null) {
      new RootLocalDriver(cell, chassis, node)
    } else {
      new LocalDriver(cell, chassis, node, Some(parent))
    }
  }

  def create(chassis: Chassis, cell: VertexHeader, parent: Driver): Driver = {
    val driver = this.createDriver(chassis, cell, parent)
    driver
  }
}

class BasicDriverFactory extends DriverFactory {
  val node = LocalNode()

  protected override def createDriver(chassis: Chassis, cell: VertexHeader, parent: Driver): Driver = {
    this.createLocalDriver(chassis, cell, node, parent)
  }

}

object DriverFactory {
  val default = new BasicDriverFactory()
}
