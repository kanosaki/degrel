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
    pattern == Label.V.wildcard ||
      this.expr == pattern.expr
  }

  override def compare(that: Label): Int = {
    this.expr.compare(that.expr)
  }

  def symbol: Symbol = Symbol(this.expr)

  def isMeta: Boolean = this.expr.startsWith("__")
}

object Label {
  protected val cache = mutable.HashMap[Symbol, Label]()

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

  object V {
    val wildcard = Label(SpecialLabels.V_WILDCARD)
    val reference = Label(SpecialLabels.V_REFERENCE)
    val cell = Label(SpecialLabels.V_CELL)
    val rule = Label(SpecialLabels.V_RULE)
    val vTrue = Label("true")
    val vFalse = Label("false")
    val value = Label(SpecialLabels.V_VALUE)
  }

  val Vertex = V

  object E {
    val tag = Label(SpecialLabels.E_TAG)
    val rhs = Label(SpecialLabels.E_RHS)
    val lhs = Label(SpecialLabels.E_LHS)
    val ref = Label(SpecialLabels.E_REFERENCE_TARGET)
    val others = Label(SpecialLabels.E_OTHERS)
    val include = Label(SpecialLabels.E_INCLUDE)

    val cellRule = Label(SpecialLabels.E_CELL_RULE)
    val cellItem = Label(SpecialLabels.E_CELL_ITEM)
    val cellBase = Label(SpecialLabels.E_CELL_BASE)
  }

  val Edge = E

  object A {
    val capturedAs = Label(SpecialLabels.A_CAPTURED_AS)
  }

  val Attributes = A

  // name :: List[Symbol]
  object N {
    val main = List(SpecialLabels.N_MAIN)
  }

  val Name = N

  def convertAttrMap(origin: Iterable[(Label, String)]): Map[Label, String] = {
    origin.map {
      case (k, v) => k -> v
    }.toMap
  }
}

case class BasicLabel(sym: Symbol) extends Label {
  override def expr: String = sym.name

  override def symbol = sym
}



