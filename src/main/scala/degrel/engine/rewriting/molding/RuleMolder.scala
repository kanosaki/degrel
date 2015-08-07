package degrel.engine.rewriting.molding

import degrel.core.{Vertex, VertexHeader}
import degrel.engine.ContinueRule

class RuleMolder(val mold: Vertex, val context: MoldingContext) extends Molder {
  override val header: VertexHeader = context.getHeader(mold)

  override def onPhase(ph: MoldPhase): Unit = ph match {
    case MoldPhase.Mold => {
      this.doMold()
    }
    case _ =>
  }

  // Perform as RhsVertex
  /**
   * 自身を書き換え右辺の頂点として，新規に頂点を構成します
   * 自分が左辺でマッチしている頂点，すなわち対応するredex上の頂点を持つ場合は，頂点をマージします
   * また，マッチしていない場合は新規に頂点を構築します
   */
  private def doMold(): Unit = {
    val contRule = new ContinueRule(mold.asRule, context.binding)
    this.header.write(contRule)
  }

  // Process only cell items
  override val children: Iterable[Molder] = Seq()
}
