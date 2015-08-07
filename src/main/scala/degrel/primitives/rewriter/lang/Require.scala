package degrel.primitives.rewriter.lang

import degrel.core.VertexHeader
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

class Require extends Rewriter {
  override def rewrite(self: Driver, target: VertexHeader): RewriteResult = ???

  override def pp(implicit opt: PrettyPrintOptions): String = ???
}
