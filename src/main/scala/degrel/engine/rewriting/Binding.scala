package degrel.engine.rewriting

import degrel.core
import degrel.core.{Edge, Vertex, Element}

object Binding {
  def apply(bridges: Seq[MatchBridge[Element]], parent: Binding = null) = {
    if (parent == null) {
      new Binding(bridges)
    } else {
      new ChainedBinding(bridges, parent)
    }
  }
}

class Binding(private[rewriting] val bridges: Seq[MatchBridge[Element]]) extends Map[Element, Element] {
  protected val map: Map[Element, Element] = bridges.map(br => (br._1, br._2)).toMap

  private val unmatchedEdgesTable: Map[Vertex, Iterable[Edge]] = bridges.flatMap {
    case vb: VertexBridge => List(vb.dataVertex -> vb.notMatchedEdges)
    case _ => Nil
  }.toMap

  def unmatchedEdges(v: Vertex): Iterable[Edge] = {
    unmatchedEdgesTable.getOrElse(v, Seq())
  }

  def get(key: Element): Option[Element] = map.get(key)

  def iterator: Iterator[(Element, Element)] = map.iterator

  def -(key: Element): Map[Element, Element] = map - key

  def +[B1 >: Element](kv: (Element, B1)): Map[Element, B1] = map + kv

  def asQueryable: QueryableBinding = {
    new QueryableBinding(bridges)
  }

  def confirm(): Boolean = {
    bridges.forall(br => br.confirm())
  }
}

class ChainedBinding(_bridges: Seq[MatchBridge[Element]], val parent: Binding) extends Binding(_bridges) {
  require(parent != null)

  override def get(key: Element): Option[Element] = map.get(key) match {
    case Some(v) => Some(v)
    case None => parent.get(key)
  }

  override def iterator: Iterator[(Element, Element)] = map.iterator ++ parent.iterator
}

/**
 * For debugging
 */
class QueryableBinding(bridges: Seq[MatchBridge[Element]]) extends Binding(bridges) {

  def queryPatternVertices(f: core.Vertex => Boolean): Iterable[core.Vertex] = {
    map.filter {
      case (v: core.Vertex, _) => f(v)
      case _ => false
    }.map {
      case (v: core.Vertex, _) => v
      case _ => throw new Exception("Illegal state")
    }
  }

  def queryDataVertices(f: core.Vertex => Boolean): Iterable[core.Vertex] = {
    map.filter {
      case (_, v: core.Vertex) => f(v)
      case _ => false
    }.map {
      case (_, v: core.Vertex) => v
      case _ => throw new Exception("Illegal state")
    }
  }

  def query(patternPredicate: core.Vertex => Boolean): Iterable[(core.Vertex, core.Vertex)] = {
    map.filter {
      case (v: core.Vertex, _) => patternPredicate(v)
      case _ => false
    }.map {
      case (pat: core.Vertex, data: core.Vertex) => (pat, data)
      case _ => throw new Exception("Illegal state")
    }
  }


  override def asQueryable: QueryableBinding = this
}
