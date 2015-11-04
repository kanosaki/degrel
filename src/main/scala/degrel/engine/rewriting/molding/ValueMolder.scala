package degrel.engine.rewriting.molding

import degrel.core.{Vertex, VertexHeader}

/**
 * `ValueVertex`のための`Molder`
 * @param mold Moldとなる`ValueVertex`の`VertexHeader`
 * @param context 使用する`MoldingContext`
 */
class ValueMolder(val mold: Vertex, val context: MoldingContext) extends Molder {
  override val header: VertexHeader = mold.asHeader
}
