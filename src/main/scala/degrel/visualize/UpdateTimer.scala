package degrel.visualize

import javafx.animation.AnimationTimer

abstract class UpdateTimer(val fps: Double) extends AnimationTimer {
  var previousUpdate: Long = 0
  var intervalNanoSec: Long = ((1000 * 1000 * 1000) * (1.0 / fps)).toLong

  override def handle(now: Long): Unit = {
    if ((now - previousUpdate) < intervalNanoSec) return
    update(now)
    previousUpdate = now
  }

  def update(now: Long): Unit
}
