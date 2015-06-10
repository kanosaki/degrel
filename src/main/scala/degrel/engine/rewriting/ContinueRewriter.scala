package degrel.engine.rewriting

import degrel.core.{Vertex, VertexHeader, Rule}
import degrel.engine.Driver

/**
 * 続きからの書き換えを行います
 * @param rule 続けて書き換えを行う`Rule`
 * @param continuation 以前の環境を含む`Continuation`
 * @note `A -> B -> C`という規則の`B -> C`を継続する場合は，`'->'(B, C)`を指す`Rule`を渡す必要があります
 */
class ContinueRewriter(val rule: Rule, val continuation: Continuation) extends BasicRewriter {
  override protected def getBinding(pack: BindingPack): Binding = {
    val currentBinding = this.pick(pack)
    val prevBinding = continuation match {
      case Continuation.Continue(_, pBind) => pBind
    }
    new ChainedBinding(currentBinding.bridges, prevBinding)
  }

  override def applyResult(target: VertexHeader, parent: Driver, builtGraph: Vertex): Unit = {
    parent.cell.removeRoot(target)
    parent.send(builtGraph)
  }
}
