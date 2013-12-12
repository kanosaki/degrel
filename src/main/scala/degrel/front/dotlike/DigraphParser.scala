package degrel.front.dotlike

import scala.util.parsing.combinator.RegexParsers

/**
 * DOT言語ライクな記法で一つの根付きグラフを記述します．
 * @example @rootlabel {
 *          shino -> alice : friend
 *          shino -> 16 : age
 *          shino { height: 155, boodtype: A }
 *          }
 *
 */
object DigraphParser extends RegexParsers {
  val PAT_ATTR_VALUE = """[^,}]*""".r
  val PAT_ATTR_KEY = """[^:]+""".r

  def label: Parser[String] = ???

  def header: Parser[String] = "@" ~> label

  def body = "{" ~> diElems <~ "}" ^^ AstDigraphBody

  def elemSep = ";" | "\n"

  def diElems: Parser[Seq[AstDigraphElement]] = repsep(diElem, elemSep)

  def diElem = label ~ (edge_ | attr_) ^^ {
    case lbl ~ AstDiEdgePiece(dst, edgeLbl) => AstDigraphEdge(lbl, dst, edgeLbl)
    case lbl ~ AstDiAttrPiece(attrs) => AstDigraphAttributes(lbl, attrs)
  }

  def edge_ : Parser[AstDiEdgePiece] = "->" ~> label ~ ":" ~ label ^^ {
    case dstLabel ~ _ ~ edgeLabel => AstDiEdgePiece(dstLabel, edgeLabel)
  }

  /**
   * 属性のパーサー
   */
  def attribute: Parser[(String, String)] = PAT_ATTR_KEY ~ ":" ~ PAT_ATTR_VALUE ^^ {
    case key ~ _ ~ value => (key, value)
  }

  def attr_ : Parser[AstDiAttrPiece] = "{" ~> repsep(attribute, ",") <~ "}" ^^ {
    case ss => AstDiAttrPiece(ss.toMap)
  }

  def digraph: Parser[AstDigraph] = header ~ body ^^ {
    case hd ~ bd => AstDigraph(hd, bd)
  }
}
