package degrel.front

import degrel.core.ValueVertex

/**
 * `ValueVertex`として頂点を生成するAST
 * @tparam T 内包するデータの型`T`, `ValueVertex[T]`と対応します
 */
trait AstValueVertex[T] extends AstVertex {
  def toVertex: ValueVertex[T]
}

/**
 * 整数値を保持する`ValueVertex`のためのASTです
 */
case class AstIntegerVertex(expr: String, radix: Int = 10) extends AstValueVertex[Int] {
  override def toVertex: ValueVertex[Int] = {
    new ValueVertex(expr.toInt)
  }
}

/**
 * 浮動小数値を保持する`ValueVertex`のためのASTです
 */
case class AstFloatVertex(expr: String) extends AstValueVertex[Double] {
  override def toVertex: ValueVertex[Double] = {
    new ValueVertex(expr.toDouble)
  }
}

/**
 * 文字列を保持する`ValueVertex`のためのASTです
 * `stringLiteral`はexprとしてダブルクオーテーションで囲った物を返してくるので
 * 取り除きます
 */
case class AstStringVertex(expr: String, trimQuotes: Boolean) extends AstValueVertex[String] {
  override def toVertex: ValueVertex[String] = {
    if (trimQuotes) {
      // check
      require(expr.charAt(0) == '"')
      require(expr.charAt(expr.length - 1) == '"')

      new ValueVertex(expr.substring(1, expr.length - 1))
    } else {
      new ValueVertex(expr)
    }
  }
}
