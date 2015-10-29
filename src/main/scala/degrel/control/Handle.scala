package degrel.control

import degrel.engine.{LocalDriver, Chassis}

/**
  * Controls Chassis
  */
trait Handle {
  def current: LocalDriver

  def chassis: Chassis
}
