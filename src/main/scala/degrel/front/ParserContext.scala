package degrel.front

import scala.collection.mutable

class ParserContext(val parent: ParserContext = ParserContext.default) {
  protected val operators = new mutable.HashMap[String, BinOp]()

  def addOperator(expr: String, precedence: Int = 0, associativity: OpAssoc = OpAssoc.Left): Unit = {
    operators += expr -> new BinOp(expr, precedence, associativity)
  }

  /**
   * 演算子が定義されているかを返します．多重定義がある場合は例外を送出します．
   * @param expr 演算子を表す文字列
   * @return 見つかった演算子
   */
  def findOp(expr: String): Option[BinOp] = {
    val fromParent = parent.findOp(expr)
    val fromThis = operators.get(expr)
    (fromThis, fromParent) match {
      case (Some(_), Some(_)) => throw new CodeException("Duplicated operator definition")
      case (p, t) => p.orElse(t)
    }
  }
}

class DefaultParserContext extends ParserContext(null) {
  operators ++= BinOp.builtins.map(op => (op.expr, op))

  override def findOp(expr: String) = operators.get(expr)
}

object ParserContext {
  private val _default = new DefaultParserContext()

  def default = _default
}
