package degrel.front

trait AstLiteral extends AstNode {

}

case class AstLabel(expr: String) extends AstLiteral {

}

case class AstVertexBinding(expr: String) extends AstLiteral {

}

case class AstName(label: Option[AstLabel], capture: Option[AstVertexBinding]) {
  def labelExpr: Option[String] = label.flatMap(l => Some(l.expr))

  def captureExpr: Option[String] = capture.flatMap(c => Some(c.expr))

  def exprPair: (Option[String], Option[String]) = (labelExpr, captureExpr)
}

case class AstAttribute(key: String, value: String) extends AstLiteral {

}

case class AstBinOp(expr: String)
                   (implicit ctx: ParserContext) extends AstLiteral with Comparable[AstBinOp]  {
  val op = ctx.findOp(expr) match {
    case Some(o) => o
    case None =>
      throw new CodeException(s"Undefined operator $expr")
  }

  override def equals(other: Any) = other match {
    case o: AstBinOp => o.op == this.op
    case _ => false
  }

  override def compareTo(o: AstBinOp): Int = {
    op.compareTo(o.op)
  }
}

