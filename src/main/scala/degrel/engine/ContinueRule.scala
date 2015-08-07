package degrel.engine

import degrel.core._
import degrel.engine.rewriting.Binding

class ContinueRule(val baseRule: Rule, val binding: Binding) extends VertexBody with Rule {
  override def rhs: Vertex = baseRule.rhs

  override def lhs: Vertex = baseRule.lhs

  override def edges: Iterable[Edge] = baseRule.edges

  override def attributes: Map[Label, String] = baseRule.attributes

  override def shallowCopy(): Vertex = baseRule.shallowCopy()

  override def label: Label = baseRule.label

  override def attr(key: Label): Option[String] = baseRule.attr(key)
}
