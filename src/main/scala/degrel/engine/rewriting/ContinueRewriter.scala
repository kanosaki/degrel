package degrel.engine.rewriting

import degrel.core.{Edge, Rule, Vertex, VertexHeader}
import degrel.engine.Driver
import degrel.utils.PrettyPrintOptions

/**
 * 続きからの書き換えを行います
 * @param rule 続けて書き換えを行う`Rule`
 * @param continuation 以前の環境を含む`Continuation`
 * @note `A -> B -> C`という規則の`B -> C`を継続する場合は，`'->'(B, C)`を指す`Rule`を渡す必要があります
 */
class ContinueRewriter(val rule: Rule, val continuation: Continuation) extends BasicRewriter {
  val continue = continuation.asInstanceOf[Continuation.Continue]

  override protected def getBinding(pack: BindingPack, cellBinding: Binding): Binding = {
    val currentBinding = this.pick(pack)
    val prevBinding = if (cellBinding.nonEmpty) {
      new ChainedBinding(continue.binding.bridges, cellBinding)
    } else {
      continue.binding
    }
    new ChainedBinding(currentBinding.bridges, prevBinding)
  }

  override def applyResult(target: VertexHeader, parent: Driver, builtGraph: Vertex): Unit = {
    parent.cell.removeRoot(target)
    parent.send(builtGraph)
  }

  override def pp(implicit opt: PrettyPrintOptions): String = {
    if (opt.multiLine) {
      val bindingPp = continue.binding.map {
        case (k, v) => {
          val prefix = k match {
            case k: Edge => "E"
            case k: Vertex => "V"
          }
          s"$prefix: ${k.pp} --- ${v.pp}"
        }
      }.mkString("\n  ")
      s"${this.rule.pp} [\n  $bindingPp\n]"
    } else {
      s"${this.rule.pp} ${continue.binding.toString()}"
    }
  }
}
