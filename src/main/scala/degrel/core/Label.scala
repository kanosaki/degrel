package degrel.core

import scala.language.implicitConversions

trait Label extends Ordered[Label] {
  def expr: String

  override def equals(other: Any) = other match {
    case l: Label => l.expr == this.expr
    case s: String => s == this.expr
    case s: Symbol => s.name == this.expr
    case _ => false
  }

  override def hashCode(): Int = {
    expr.hashCode()
  }

  def matches(pattern: Label) = {
    pattern.expr == WildcardLabel.expr ||
      this.expr == pattern.expr
  }

  override def compare(that: Label): Int = {
    this.expr.compare(that.expr)
  }
}

object Label {
  val specials: Map[String, Label] =
    Map(
      "_" -> WildcardLabel,
      "@" -> ReferenceLabel)
  val wildcard = WildcardLabel
  val reference = ReferenceLabel

  implicit def apply(expr: String): Label = {
    specials.get(expr) match {
      case None => BasicLabel(expr)
      case Some(s) => s
    }
  }
}

case class BasicLabel(expr: String) extends Label {
}

case object WildcardLabel extends Label {
  val expr: String = "_"
}

case object ReferenceLabel extends Label {
  val expr: String = "@"
}


