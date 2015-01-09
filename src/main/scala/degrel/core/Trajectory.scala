package degrel.core

import scala.collection.mutable

class Trajectory {
  protected val trajectory = new mutable.HashSet[Vertex]()

  def walk[E](v: Vertex)(f: PartialFunction[TrajectoryStamp, E]): E = {
    if (!this.isStamped(v)) {
      this.push(v)
      f(Unvisited(this))
    } else {
      f(Visited(this))
    }
  }

  def push(v: Vertex): Unit = {
    trajectory += v
  }

  def isStamped(v: Vertex): Boolean = trajectory.contains(v)
}


trait TrajectoryStamp
case class Visited(trj: Trajectory) extends TrajectoryStamp {

}

case class Unvisited(trj: Trajectory) extends TrajectoryStamp {

}
