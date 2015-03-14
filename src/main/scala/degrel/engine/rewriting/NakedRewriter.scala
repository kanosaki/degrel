package degrel.engine.rewriting

import degrel.core.{Rule, Vertex, VertexHeader}

/**
 * 継続の情報等を一切持たない`Rule`だけに依存した書き換えを行う`Rewriter`です
 */
class NakedRewriter(val rule: Rule) extends Rewriter {
  override protected def getBinding(pack: BindingPack): Binding = {
    this.pick(pack)
  }
}
