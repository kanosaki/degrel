package degrel.front

import degrel.core.Cell

/**
 * Cellを表すAST
 */
case class AstCell(items: Seq[AstCellItem]) extends AstExpr[Cell] {
  /**
   * Import句
   */
  val imports = items.flatMap {
    case i: AstImport => Some(i)
    case _ => None
  }

  /**
   * fin句
   */
  val fins = items.flatMap {
    case i: AstFin => Some(i)
    case _ => None
  }

  def roots: Seq[AstVertex] = {
    items.flatMap {
      case g: AstVertex => Some(g)
      case _ => None
    }
  }

  def edges: Seq[AstCellEdge] = items.collect {
    case ce: AstCellEdge => ce
  }
}

trait AstCellItem extends AstNode {
}

case class AstCellEdge(label: AstLabel, dst: AstVertex) extends AstCellItem

case class AstImport(from: Option[AstLabel],
                     imports: Seq[AstLabel],
                     as: Option[AstLabel] = None) extends AstCellItem {
  if (imports.size > 1 && as.isDefined) {
    // 複数インポートとインポートしたcellのリネームは同時に行えません
    throw new SyntaxError(
      "Multi import and module rename cannot use at the same time. " +
        "Use 'import A as B; import X as Y'")
  }
}

case class AstFin(expr: AstVertex)(implicit ctx: ParserContext) extends AstCellItem {

}

