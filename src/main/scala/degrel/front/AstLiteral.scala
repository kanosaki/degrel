package degrel.front

trait AstLiteral extends AstNode {

}

case class AstLabel(expr: String) extends AstLiteral {

}

case class AstBinding(expr: String) extends AstLiteral {

}

case class AstName(label: Option[AstLabel], capture: Option[AstBinding]) {
  def labelExpr: Option[String] = label.flatMap(l => Some(l.expr))

  def captureExpr: Option[String] = capture.flatMap(c => Some(c.expr))

  def exprPair: (Option[String], Option[String]) = (labelExpr, captureExpr)
}

case class AstAttribute(key: String, value: String) extends AstLiteral {

}

/**
 * 2項演算子を表すASTです．二項演算子は現れる文脈によってその結合方向と優先順位が変化します．
 * その現在のコンテキストを`ParserContext`で受け取ります
 * @param expr 二項演算子の表現
 * @param ctx 現在の構文定義コンテキスト
 */
case class AstBinOp(expr: String)
                   (implicit ctx: ParserContext) extends AstLiteral with Comparable[AstBinOp]  {
  val op = ctx.findOp(expr) match {
    case Some(o) => o
    case None =>
      throw new CodeException(s"Undefined operator '$expr'")
  }

  override def equals(other: Any) = other match {
    case o: AstBinOp => o.op == this.op
    case _ => false
  }

  override def compareTo(o: AstBinOp): Int = {
    op.compareTo(o.op)
  }
}

