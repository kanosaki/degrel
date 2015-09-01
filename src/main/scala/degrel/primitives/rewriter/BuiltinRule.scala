package degrel.primitives.rewriter

import degrel.core._

class BuiltinRule extends Rule {
  override def rhs: Vertex = ???

  override def lhs: Vertex = ???

  override def edges: Iterable[Edge] = ???

  override def attributes: Map[Label, String] = ???

  override def shallowCopy(): Vertex = ???

  override def label: Label = ???

  override def attr(key: Label): Option[String] = ???

  override def id: ID = ???

  override def fingerprintCache: Long = ???

  override def fingerprintCache_=(fp: Long): Unit = ???
}
