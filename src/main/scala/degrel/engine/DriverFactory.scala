package degrel.engine

import degrel.core.Vertex

/**
 * Driverを作成します．この段階では`Chassis`への登録は行われていません
 */
class DriverFactory {
  def create(chassis: Chassis, cell: Vertex, parent: LocalDriver = null): LocalDriver = {
    new LocalDriver(cell, chassis, parent)
  }
}

class RootHashDriverFactory extends DriverFactory {
  override def create(chassis: Chassis, cell: Vertex, parent: LocalDriver = null): LocalDriver = {
    val driver = new LocalDriver(cell, chassis, parent)
    driver.rewritee = new RootTableRewriteeSet(driver)
    driver
  }
}

object DriverFactory {
  val default = new DriverFactory()
}
