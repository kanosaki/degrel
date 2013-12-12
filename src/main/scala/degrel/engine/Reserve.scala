package degrel.engine

import degrel.core.{Vertex, Rule, Traverser}
import scala.collection.mutable

trait Reserve {
  def rewriters: Iterable[Rewriter]

  def iterVertices: Iterable[Vertex] = {
    roots.flatMap(Traverser(_))
  }

  def roots: Iterable[Vertex]

  def rewriteStep() = {
    var rewrote = false
    for (rw <- this.rewriters) {
      rewrote ||= rw.step(this)
    }
    rewrote
  }

  def rewriteUntilStop = {
    while (this.rewriteStep()) {}
  }

  def freeze: Reserve = {
    new FrozenReserve(this.rewriters.toSeq, this.roots.map(_.freeze).toSeq)
  }
}

class LocalReserve extends Reserve {
  private val _roots = new mutable.ListBuffer[Vertex]()
  private val _rewriters = new mutable.ListBuffer[Rewriter]()

  def roots = _roots.toSeq

  def rewriters = _rewriters.toSeq

  def addRule(rule: Rule) = {
    _rewriters += new Rewriter(rule)
  }

  def addRules(rules: TraversableOnce[Rule]) = {
    _rewriters ++= rules.map(new Rewriter(_))
  }

  def addVertex(v: Vertex) = {
    _roots += v
  }

  def addVertices(vs: TraversableOnce[Vertex]) = {
    _roots ++= vs
  }
}

class FrozenReserve(val _rewriters: Iterable[Rewriter], val _roots: Iterable[Vertex]) extends Reserve {
  def rewriters: Iterable[Rewriter] = _rewriters

  def roots: Iterable[Vertex] = _roots
}

