package degrel.primitives.rewriter.lang

import degrel.core.Vertex
import degrel.engine.rewriting.{RewritingTarget, RewriteResult, Rewriter, RewritingTarget$}
import degrel.utils.PrettyPrintOptions

class Require extends Rewriter {
  override def rewrite(rc: RewritingTarget): RewriteResult = ???

  override def pp(implicit opt: PrettyPrintOptions): String = ???

  override def pattern: Vertex = ???
}
