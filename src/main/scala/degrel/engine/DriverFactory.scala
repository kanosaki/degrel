package degrel.engine

import degrel.core.Vertex

/**
 * Driverを作成します．この段階では`Chassis`への登録は行われていません
 */
class DriverFactory {
  def create(chassis: Chassis, cell: Vertex, parent: Driver = null): Driver = {
    new Driver(cell, chassis, parent)
  }
}

class RootHashDriverFactory extends DriverFactory {
  override def create(chassis: Chassis, cell: Vertex, parent: Driver = null): Driver = {
    val driver = new Driver(cell, chassis, parent)
    driver.rewritee = new RootTableRewriteeSet(driver)
    driver
  }
}

object DriverFactory {
  val default = new DriverFactory()
}
