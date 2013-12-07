package degrel.engine

import degrel.core.Element

import scalaz._
import Scalaz._

trait BindingPack {
  def join(other: BindingPack): BindingPack

  def ++(other: BindingPack) = this.join(other)

  def pick: Binding = {
    Binding(this.pickBranch(new PickOption()))
  }

  def pickBranch(pickOpt: PickOption) : Seq[MatchBridge[Element]]
}

case class MonoBindingPack(bridges: Seq[MatchBridge[Element]]) extends BindingPack {
  def join(other: BindingPack) = {
    other match {
      case MonoBindingPack(brs) => MonoBindingPack(brs ++ bridges)
      case pb: PolyBindingPack => pb.join(this)
    }
  }

  def pickBranch(pickOpt: PickOption): Seq[MatchBridge[Element]] = this.bridges
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

  def pickBranch(pickOpt: PickOption): Seq[MatchBridge[Element]] = {
    bindings.head.pickBranch(pickOpt)
  }
}
class PickOption {
}

object Binding {
  def apply(bridges: Seq[MatchBridge[Element]]) = {
    new Binding(bridges.map(br => (br._1, br._2)).toMap)
  }
}

class Binding(private val map: Map[Element, Element]) extends Map[Element, Element] {
  def get(key: Element): Option[Element] = map.get(key)

  def iterator: Iterator[(Element, Element)] = map.iterator

  def -(key: Element): Map[Element, Element] = map - key

  def +[B1 >: Element](kv: (Element, B1)): Map[Element, B1] = map + kv
}
