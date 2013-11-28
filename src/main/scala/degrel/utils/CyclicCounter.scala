package degrel.utils

class CyclicCounter(val cycle: Int) {
  assert(cycle > 0, "Cycle count should be >= 1")

  private var current = cycle

  def reset() {
    current = cycle
  }

  def proceedCount(): Int = {
    current -= 1
    current
  }

  def next(): Boolean = {
    if (current == 0) {
      this.reset()
    }
    this.proceedCount()
    current == 0
  }
}
