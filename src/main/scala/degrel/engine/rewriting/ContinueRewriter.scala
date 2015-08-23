package degrel.engine.rewriting

import degrel.core.{Edge, Rule, Vertex}
import degrel.utils.PrettyPrintOptions

/**
 * 続きからの書き換えを行います
 * @param rule 続けて書き換えを行う`Rule`
 * @note `A -> B -> C`という規則の`B -> C`を継続する場合は，`'->'(B, C)`を指す`Rule`を渡す必要があります
 */
class ContinueRewriter(val rule: Rule, val binding: Binding, val tempVertex: Vertex) extends BasicRewriter {

  override protected def getBinding(pack: BindingPack, cellBinding: Binding): Binding = {
    val currentBinding = this.pick(pack)
    val prevBinding = if (cellBinding.nonEmpty) {
      new ChainedBinding(binding.bridges, cellBinding)
    } else {
      binding
    }
    new ChainedBinding(currentBinding.bridges, prevBinding)
  }

  override def pp(implicit opt: PrettyPrintOptions): String = {
    if (opt.multiLine) {
      val bindingPp = binding.map {
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
      s"${this.rule.pp} ${binding.toString()}"
    }
  }
}
