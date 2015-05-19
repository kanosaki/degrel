package degrel.primitives.rewriter.lang

import degrel.core.VertexHeader
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

class Require extends Rewriter {
  override def rewrite(target: VertexHeader, parent: Driver): RewriteResult = ???

  override def pp(implicit opt: PrettyPrintOptions): String = ???
}
