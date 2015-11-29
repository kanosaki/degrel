package degrel.cluster

import degrel.core.{VertexHeader, Vertex}
import degrel.engine._

/**
  * AkkaのConfigや，SessionManager等管理Actorからの情報を受け取って動作する
  * DriverFactoryです
  */
class ClusterDriverFactory(node: LocalNode) extends DriverFactory {
  override protected def createDriver(chassis: Chassis, cell: VertexHeader, parent: Driver): Driver = {
    val localDriver = this.createLocalDriver(chassis, cell, node, parent)
    // TODO: designate which node to spawn on.
    node.registerDriver(localDriver.id.ownerID, localDriver)
    localDriver
  }
}

object ClusterDriverFactory {
  def apply(node: LocalNode): ClusterDriverFactory = {
    new ClusterDriverFactory(node)
  }
}
