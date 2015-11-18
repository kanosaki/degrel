package degrel.control

import degrel.engine.Driver

abstract class HandleBase extends Handle {
  protected var _current: Driver = null

  override def current: Driver = {
    _current
  }

  def setCurrent(drv: Driver) = {
    _current = drv
  }

  def currentName: String = {
    if (this.current == null) {
      "<null>"
    } else {
      this.chassis.getName(this.current)
    }
  }
}
