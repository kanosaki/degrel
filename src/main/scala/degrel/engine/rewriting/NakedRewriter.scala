package degrel.engine.rewriting

import degrel.core.Rule

/**
 * 継続の情報等を一切持たない`Rule`だけに依存した書き換えを行う`Rewriter`です
 */
class NakedRewriter(val rule: Rule) extends BasicRewriter {
  protected def getBinding(pack: BindingPack, cellBinding: Binding): Binding = {
    val current = this.pick(pack)
    new ChainedBinding(current.bridges, cellBinding)
  }
}
