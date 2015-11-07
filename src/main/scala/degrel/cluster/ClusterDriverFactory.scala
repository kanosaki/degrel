package degrel.cluster

import degrel.core.Vertex
import degrel.engine.{LocalDriver, Driver, Chassis, DriverFactory}

/**
  * AkkaのConfigや，SessionManager等管理Actorからの情報を受け取って動作する
  * DriverFactoryです
  */
class ClusterDriverFactory(node: LocalNode) extends DriverFactory {
  override protected def createDriver(chassis: Chassis, cell: Vertex, parent: Driver): LocalDriver = {
    new LocalDriver(cell, chassis, node, parent)
  }
}

object ClusterDriverFactory {
  def apply(node: LocalNode): ClusterDriverFactory = {
    new ClusterDriverFactory(node)
  }
}
