package degrel.engine.rewriting

import degrel.core.Rule

class ContinueRewriter(val rule: Rule, val continuation: Continuation) extends Rewriter {
  override protected def getBinding(pack: BindingPack): Binding = {
    val currentBinding = this.pick(pack)
    val prevBinding = continuation match {
      case Continuation.HasNext(_, pBind) => pBind
    }
    new ChainedBinding(currentBinding.bridges, prevBinding)
  }
}
