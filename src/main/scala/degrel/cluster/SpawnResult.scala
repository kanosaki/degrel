package degrel.cluster

import degrel.engine.Driver

trait SpawnResult {
  def isSucceed: Boolean
}

trait SpawnResultSuccess extends SpawnResult {
  override def isSucceed: Boolean = true

  def result: Driver
}

trait SpawnResultFailure extends SpawnResult {
  override def isSucceed: Boolean = false
}

object SpawnResult {

  case class LocalSpawned(result: Driver) extends SpawnResultSuccess

  case class RemoteSpawned(result: Driver) extends SpawnResultSuccess

  case class NoVacantNode() extends SpawnResultFailure

  case class OtherError(th: Throwable) extends SpawnResultFailure

}



