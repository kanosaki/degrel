package degrel.core

trait Freezable {
  def freeze: this.type
  def isFrozen: Boolean
}

trait SwitchingFreezable extends Freezable {
  private var _isFrozen = false

  def isFrozen: Boolean = _isFrozen
  def freeze = {
    _isFrozen = true
    this
  }
}
