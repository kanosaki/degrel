package degrel.core

import scala.collection.mutable

class TraverseHistory {
  private val history = new mutable.HashSet[Vertex]()

  def push(v: Vertex) : Unit = {
    history += v
  }

  def next[E](v: Vertex)(f: PartialFunction[Either[TraverseHistory, TraverseHistory], E]): E = {
    if(!history.contains(v)) {
      this.push(v)
      f(Right(this))
    } else {
      f(Left(this))
    }
  }
}
