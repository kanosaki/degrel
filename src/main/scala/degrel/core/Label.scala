package degrel.core

import scala.collection.mutable
import scala.language.implicitConversions

trait Label extends Ordered[Label] {
  def expr: String

  override def equals(other: Any) = other match {
    case l: Label => (this eq l) || l.expr == this.expr
    case s: String => s == this.expr
    case s: Symbol => s.name == this.expr
    case _ => false
  }

  // 191 :: a prime number
  override def hashCode(): Int = 191 + expr.hashCode

  def matches(pattern: Label) = {
    pattern == Label.wildcard ||
      this.expr == pattern.expr
  }

  override def compare(that: Label): Int = {
    this.expr.compare(that.expr)
  }

  def symbol: Symbol = Symbol(this.expr)
}

object Label {
  protected val cache = mutable.HashMap[Symbol, Label]()

  val wildcard = Label(SpecialLabels.V_WILDCARD)
  val reference = Label(SpecialLabels.V_REFERENCE)

  val rhs = Label(SpecialLabels.E_RHS)
  val lhs = Label(SpecialLabels.E_LHS)

  implicit def apply(sym: Symbol): Label = {
    cache.get(sym) match {
      case Some(l) => l
      case None => {
        val lbl = BasicLabel(sym)
        cache += sym -> lbl
        lbl
      }
    }
  }

  implicit def apply(expr: String): Label = {
    this(Symbol(expr))
  }
}

case class BasicLabel(sym: Symbol) extends Label {
  override def expr: String = sym.name

  override def symbol = sym
}



