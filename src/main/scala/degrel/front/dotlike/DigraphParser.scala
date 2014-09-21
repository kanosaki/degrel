package degrel.front.dotlike

import degrel.front.SyntaxError

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
  override val skipWhitespace = false
  override val whiteSpace = """[ \t]+""".r
  val ws = """[ \t]*""".r
  val PAT_ATTR_VALUE = """[^,}]*""".r
  val PAT_ATTR_KEY = """[^:]+""".r
  val IDENTIFIER_SEP = "$"

  def eol = sys.props("line.separator")

  def eoi = """\z""".r // End of Input

  def token[T](p: Parser[T]): Parser[T] = ws ~> p

  def label: Parser[String] = token( """[_a-z0-9][_a-z0-9A-Z]*""".r)

  /**
   * 各頂点の識別子．基本的に`label`の形を取りますが，グラフ内に同じラベルを持つ別の頂点を作成したい場合は
   * `label$label`のようにして別途修飾し区別することが可能です．
   * foobar, foobar$hoge, foobar$piyo
   * はすべて`foobar`という頂点を生成しますが，別のものと区別されます
   * また，foo$hogeとbar$hoge は別のものととらえられます
   */
  def identifier: Parser[AstDigraphIdentifier] = opt(label) ~ opt(identifierSeparator ~> label) ^^ {
    case lbl ~ id => AstDigraphIdentifier(lbl, id)
  }

  def identifierSeparator = token(IDENTIFIER_SEP)


  def labelOrEmpty: Parser[String] = opt(label) ^^ {
    case Some(lbl) => lbl
    case None => ""
  }

  def header: Parser[String] = token("@") ~> label

  def body = token("{") ~> diElems <~ token("}") ^^ AstDigraphBody

  def elemSep = token(";" | eol)

  def diElems: Parser[Seq[AstDigraphElement]] = repsep(diElem, rep1(elemSep)).map(_.filter(_ != AstDigraphEmptyLine))

  def diElem: Parser[AstDigraphElement] = opt(edge | attr) ^^ {
    case Some(e) => e
    case None => AstDigraphEmptyLine
  }

  def edge: Parser[AstDigraphEdge] = identifier ~
    token("->") ~
    identifier ~
    token(":") ~
    label ^^ {
    case fromLabel ~ _ ~ toLabel ~ _ ~ edgeLabel => AstDigraphEdge(fromLabel,
      toLabel,
      edgeLabel)
  }

  /**
   * 属性のパーサー
   */
  def attributeEntry: Parser[(String, String)] = token(PAT_ATTR_KEY) ~
    token(":") ~
    token(PAT_ATTR_VALUE) ^^ {
    case key ~ _ ~ value => (key, value)
  }

  def attr: Parser[AstDigraphAttributes] = labelOrEmpty ~
    token("{") ~
    repsep(attributeEntry, token(",")) ~
    token("}") ^^ {
    case lbl ~ _ ~ ss ~ _ => AstDigraphAttributes(lbl, ss)
  }

  def digraph: Parser[AstDigraph] = header ~ body ^^ {
    case hd ~ bd => AstDigraph(hd, bd)
  }

  def apply(expr: String): AstDigraph = {
    parse(digraph, expr) match {
      case Success(gr, _) => gr
      case fail: NoSuccess =>
        throw new SyntaxError(fail.msg + s" in '$expr'} at ${fail.next.offset}")
    }
  }
}
