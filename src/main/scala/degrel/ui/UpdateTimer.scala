package degrel.ui

import javafx.animation.AnimationTimer

/**
 * JavaFXの描画タイマーの速度を制限して描画します．updateメソッドをオーバーライドしてください
 * @param fps updateを呼ぶ最高のfps
 */
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
