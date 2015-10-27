package degrel.front

import degrel.core.Cell

/**
 * Cellを表すAST
 */
case class AstCell(items: Vector[AstCellItem]) extends AstExpr[Cell] {
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

case class AstCellPragmaEdge(label: Option[AstLabel], dst: AstVertex) {
  def actualLabel: AstLabel = label match {
    case Some(lbl) => lbl
    case None => AstLabel(SpecialLabel.Edge.tag.name)
  }
}

case class AstCellPragma(edges: Seq[AstCellPragmaEdge]) extends AstCellItem {

}
