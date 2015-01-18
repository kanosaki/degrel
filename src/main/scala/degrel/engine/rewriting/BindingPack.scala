package degrel.engine.rewriting

import degrel.core.Element

import scalaz.Scalaz._
import scalaz._

trait BindingPack /* extends Iterable[Binding] */ {
  def join(other: BindingPack): BindingPack

  def ++(other: BindingPack) = this.join(other)

  def pickFirst: Binding = {
    val picked = this.pick(new PickOption())
    Binding(picked)
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

