package degrel.core.validate

import degrel.core._

class Validator(ruleSet: Seq[ValidationRules]) {
  def check(pred: => Boolean, msg: String) = {
  }

  def apply(target: Element, doTraverse: Boolean): Seq[ValidationFailure] = {
    val targets: Seq[Element] = (target, doTraverse) match {
      case (v: Vertex, true) => Traverser(v).toSeq
      case (v: Vertex, false) => Seq(v)
      case (g: Graph, true) => Seq(g) ++ Seq(g.vertices: _*)
      case (g: Graph, false) => Seq(g)
      case (_, true) => throw new IllegalArgumentException(s"Cannot traverse $target!")
      case (_, false) => Seq(target)
    }
    ruleSet.flatMap(_.validate(this, target))
  }
}

object Validator {
  def apply(ruleSet: Seq[ValidationRules]): Validator = {
    new Validator(ruleSet)
  }
}
