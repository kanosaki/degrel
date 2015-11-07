package degrel.engine

import degrel.cluster.LocalNode
import degrel.core.Vertex

/**
  * Driverを作成します．この段階では`Chassis`への登録は行われていません
  */
trait DriverFactory {
  def configurators: Seq[LocalDriver => Unit] = Seq()

  protected def createDriver(chassis: Chassis, cell: Vertex, parent: Driver = null): LocalDriver = {
    new LocalDriver(cell, chassis, LocalNode.current, parent)
  }

  protected def configureDriver(driver: LocalDriver): Unit = {
    configurators.foreach(_ (driver))
  }

  def create(chassis: Chassis, cell: Vertex, parent: LocalDriver = null): LocalDriver = {
    val driver = this.createDriver(chassis, cell, parent)
    this.configureDriver(driver)
    driver
  }
}

class BasicDriverFactory extends DriverFactory {
}

class RootHashDriverFactory extends DriverFactory {
  override val configurators = Seq(
    addRewritee _
  )

  protected def addRewritee(driver: LocalDriver): Unit = {
    driver.rewritee = new RootTableRewriteeSet(driver)
  }
}

object DriverFactory {
  val default = new BasicDriverFactory()
}
