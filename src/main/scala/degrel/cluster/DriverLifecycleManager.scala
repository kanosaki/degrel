package degrel.cluster

import degrel.core.{DriverState, ID, Vertex}
import degrel.engine.{Driver, LocalDriver, SpawnInfo}

import scala.collection.mutable
import scala.concurrent.stm

class DriverLifecycleManager(val owner: LocalDriver, options: LifecycleOptions) {
  def node: LocalNode = owner.node

  private var children = new mutable.HashMap[ID, Driver]()
  private val spawnInfo = mutable.HashMap[ID, SpawnInfo]()
  private val _spawnInProgressCount = stm.Ref(0)
  private val spawnQueue = mutable.ListBuffer[Vertex]()
  private val upstreams = mutable.HashMap[ID, SpawnInfo]()
  private val downstream = mutable.HashMap[ID, SpawnInfo]()

  def onStateChanged(oldState: DriverState, newState: DriverState) = {

  }

  def onNeighborUpdated() = {

  }

  def spawn(cell: Vertex) = {

  }
}

object DriverLifecycleManager {
  def apply(owner: LocalDriver, options: LifecycleOptions = LifecycleOptions()) = {
    new DriverLifecycleManager(owner, options)
  }
}

case class LifecycleOptions(autoStop: Boolean = true)
