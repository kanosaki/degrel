package degrel.control

import degrel.engine.{Chassis, Driver}

/**
 * Controls Chassis
 */
trait Handle {
  def current: Driver
  def chassis: Chassis
}
