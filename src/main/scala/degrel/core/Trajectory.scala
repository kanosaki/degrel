package degrel.core

import scala.collection.mutable

class Trajectory {
  protected val trajectory = new mutable.HashSet[Vertex]()

  def push(v: Vertex): Unit = {
    trajectory += v
  }

  def isStamped(v: Vertex): Boolean = trajectory.contains(v)

  def walk[E](v: Vertex)(f: PartialFunction[Either[this.type, this.type], E]): E = {
    if (!this.isStamped(v)) {
      this.push(v)
      f(Right(this))
    } else {
      f(Left(this))
    }
  }
}

