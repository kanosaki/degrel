package degrel.control

import degrel.engine.LocalDriver

abstract class HandleBase extends Handle {
  protected var _current: LocalDriver = null

  override def current: LocalDriver = {
    _current
  }

  def setCurrent(drv: LocalDriver) = {
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
