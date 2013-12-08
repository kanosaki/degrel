package degrel.engine

import degrel.core.Element
import degrel.core

import scalaz._
import Scalaz._

trait BindingPack extends Iterable[Binding] {
  def join(other: BindingPack): BindingPack

  def ++(other: BindingPack) = this.join(other)

  def pickFirst: Binding = {
    Binding(this.pick(new PickOption()))
  }

  def pick(pickOpt: PickOption): Seq[MatchBridge[Element]]

  def unpack: Iterable[Binding]

  def iterator: Iterator[Binding] = this.unpack.iterator
}

case class MonoBindingPack(bridges: Seq[MatchBridge[Element]]) extends BindingPack {
  def join(other: BindingPack) = {
    other match {
      case MonoBindingPack(brs) => MonoBindingPack(brs ++ bridges)
      case pb: PolyBindingPack => pb.join(this)
    }
  }

  def pick(pickOpt: PickOption): Seq[MatchBridge[Element]] = this.bridges

  def unpack: Iterable[Binding] = {
    Stream(Binding(this.pick(new PickOption())))
  }
}

case class PolyBindingPack(bindings: Iterable[BindingPack]) extends BindingPack {
  def join(other: BindingPack) = {
    other match {
      case mb: MonoBindingPack => PolyBindingPack(bindings.map(_ ++ mb))
      case pb: PolyBindingPack => {
        val src = List(this.bindings.toStream, pb.bindings.toStream).sequence.toStream.map(PolyBindingPack)
        PolyBindingPack(src)
      }
    }
  }

  def pick(pickOpt: PickOption): Seq[MatchBridge[Element]] = {
    bindings.head.pick(pickOpt)
  }

  def unpack = {
    ???
  }
}

class PickOption {
}

object Binding {
  def apply(bridges: Seq[MatchBridge[Element]]) = {
    new Binding(bridges.map(br => (br._1, br._2)).toMap)
  }
}

class Binding(protected val map: Map[Element, Element]) extends Map[Element, Element] {
  def get(key: Element): Option[Element] = map.get(key)

  def iterator: Iterator[(Element, Element)] = map.iterator

  def -(key: Element): Map[Element, Element] = map - key

  def +[B1 >: Element](kv: (Element, B1)): Map[Element, B1] = map + kv

  def asQueryable: QueryableBinding = {
    new QueryableBinding(map)
  }
}

/**
 * For debugging
 */
class QueryableBinding(protected val _map: Map[Element, Element]) extends Binding(_map) {

  def queryPatternVertices(f: core.Vertex => Boolean): Iterable[core.Vertex] = {
    _map.filter {
      case (v: core.Vertex, _) => f(v)
      case _ => false
    }.map {
      case (v: core.Vertex, _) => v
      case _ => throw new Exception("Illegal state")
    }
  }

  def queryDataVertices(f: core.Vertex => Boolean): Iterable[core.Vertex] = {
    _map.filter {
      case (_, v: core.Vertex) => f(v)
      case _ => false
    }.map {
      case (_, v: core.Vertex) => v
      case _ => throw new Exception("Illegal state")
    }
  }

  def query(patternPredicate: core.Vertex => Boolean): Iterable[(core.Vertex, core.Vertex)] = {
    _map.filter {
      case (v: core.Vertex, _) => patternPredicate(v)
      case _ => false
    }.map {
      case (pat: core.Vertex, data: core.Vertex) => (pat, data)
      case _ => throw new Exception("Illegal state")
    }
  }


  override def asQueryable: QueryableBinding = this
}
