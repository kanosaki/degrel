package degrel.engine

import degrel.core.Element

import scalaz._
import Scalaz._

trait BindingPack {
  def join(other: BindingPack): BindingPack

  def ++(other: BindingPack) = this.join(other)

  def pick: Binding = {
    new Binding(this.pickBranch(new PickOption()))
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
        val src = List(this.bindings.toStream, pb.bindings.toStream).sequence.map(PolyBindingPack).toStream
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

class Binding(bridges: Seq[MatchBridge[Element]]) {
  private val map = bridges.map(br => (br._1, br._2)).toMap
  def resolve(elem: Element) = {
    map.get(elem)
  }

  def size = map.size
}
