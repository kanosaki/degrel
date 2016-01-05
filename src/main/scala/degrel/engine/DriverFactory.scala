package degrel.engine

import com.typesafe.config.ConfigFactory
import degrel.cluster.LocalNode
import degrel.core.{VertexPin, VertexHeader, Vertex}

/**
  * Driverを作成します．この段階では`Chassis`への登録は行われていません
  */
trait DriverFactory {
  protected def createDriver(chassis: Chassis, cell: VertexHeader, reutrnTo: VertexPin, parent: Driver): Driver

  protected def createLocalDriver(chassis: Chassis, cell: VertexHeader, node: LocalNode, returnTo: VertexPin, parent: Driver): LocalDriver = {
    implicit val dispatcher = node.dispatcher
    if (parent == null) {
      new RootLocalDriver(cell, chassis, node)
    } else {
      new LocalDriver(cell, chassis, node, returnTo, Some(parent))
    }
  }

  def create(chassis: Chassis, cell: VertexHeader, returnTo: VertexPin, parent: Driver): Driver = {
    val driver = this.createDriver(chassis, cell, returnTo, parent)
    driver
  }
}

class BasicDriverFactory(node: LocalNode) extends DriverFactory {
  protected override def createDriver(chassis: Chassis, cell: VertexHeader, reutrnTo: VertexPin, parent: Driver): Driver = {
    this.createLocalDriver(chassis, cell, node, reutrnTo, parent)
  }

}

object DriverFactory {
  def default(node: LocalNode) = new BasicDriverFactory(node)
}
