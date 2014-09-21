package degrel.front

import scala.util.parsing.combinator.RegexParsers


/**
 * 項パーサー
 * foo(bar: baz) -> hoge(fuga: piyo)
 * のよう項表現をASTに変換します
 * 本来のREGRELの仕様と違い，デバッグ用に属性を持ちます
 */
object DefaultTermParser extends RegexParsers {
  /**
   * 頂点のラベルとして使えるものの正規表現
   */
  val PAT_LABEL = """([_~=.+\-*a-z0-9][_~=.+\-*a-z0-9A-Z]*)|@|(->)""".r
  /**
   * 属性の値として使える文字．任意の文字が使えるが，構文解析上属性の区切り記号と終わりの記号である
   * ','と'}'は使用できません
   * @todo エスケープできるようにする
   */
  val PAT_ATTR_VALUE = """[^,}]*""".r
  /**
   * 属性のキーとして使える文字．任意の文字が使えるが，構文解析上属性値との区切り記号の':'は除外
   * @todo エスケープできるようにする
   */
  val PAT_ATTR_KEY = """[^:]+""".r

  /**
   * 変数(Variable) varではなくcaptureという表現を使います
   * FIXME: 束縛とかいう名前の方が良いかも?
   */
  def capture: Parser[AstCapture] = """[A-Z][a-zA-Z0-9]*""".r ^^ AstCapture

  /**
   * ラベルのパーサー
   */
  def label: Parser[AstLabel] = PAT_LABEL ^^ AstLabel

  /**
   * 頂点に付くラベルと変数の組み合わせ
   */
  def name: Parser[AstName] =
    (capture ~ opt("[" ~> label <~ "]")) ^^ {
      case cap ~ lbl => AstName(Some(cap), lbl)
    } |
      label ^^ {
        case l => AstName(None, Some(l))
      }

  /**
   * 接続のパーサー
   */
  def edge: Parser[AstEdge] = label ~ ":" ~ vertex ^^ {
    case n ~ _ ~ v => AstEdge(n, v)
  }

  /**
   * 頂点の持つ接続の集合パーサー
   */
  def edges: Parser[Seq[AstEdge]] = "(" ~> repsep(edge, ",") <~ ")" ^^ {
    _.toSeq
  }

  /**
   * 属性のパーサー
   */
  def attribute: Parser[AstAttribute] = PAT_ATTR_KEY ~ ":" ~ PAT_ATTR_VALUE ^^ {
    case key ~ _ ~ value => AstAttribute(key, value)
  }

  /**
   * 頂点の持つ属性の集合パーサー
   */
  def attributes: Parser[Seq[AstAttribute]] = "{" ~> repsep(attribute, ",") <~ "}" ^^ {
    _.toSeq
  }

  def vertex: Parser[AstVertex] = name ~ opt(attributes) ~ opt(edges) ^^ {
    case n ~ attrs ~ Some(es) => AstVertex(n, attrs, es)
    case n ~ attrs ~ None => AstVertex(n, attrs, Seq())
  }

  /**
   * 単項の根のパーサー．2項演算子を含む表現が可能なので，左再帰を除去してある
   * そのため，rule_パーサーと組み合わせて使います．
   * @todo  rule_をbinop_等の一般化2項演算子パーサーに切り替える?
   *        意味解析器との連携が必要なためその辺は先送り?
   */
  def root: Parser[AstRoot] = "(" ~> root <~ ")" | vertex ~ opt(rule_) ^^ {
    case v ~ None => v
    case v ~ Some(r) => AstRule(v, r)
  }

  def rule_ : Parser[AstRoot] = BinOp.rule ~> root


  /**
   * 項表現は単項(root)の連続
   * @return
   */
  def graph: Parser[AstGraph] = root ^^ {
    case r => AstGraph(Seq(r))
  } | "(" ~> repsep(root, ",") <~ ")" ^^ {
    case rs => AstGraph(rs)
  }

  def apply(expr: String): Ast = {
    parseAll(graph, expr) match {
      case Success(gr, _) => new Ast(gr)
      case fail: NoSuccess =>
        throw new SyntaxError(fail.msg + s" in '$expr'} at ${fail.next.offset}")
    }
  }
}


