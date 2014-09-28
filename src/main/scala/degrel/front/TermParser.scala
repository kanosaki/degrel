package degrel.front

import scala.language.higherKinds
import scala.util.parsing.combinator.RegexParsers

class TermParser(val context: ParserContext = ParserContext.default) extends RegexParsers {
  /**
   * 頂点のラベルとして使えるものの正規表現
   */
  val PAT_LABEL = """([_a-z0-9A-Z][_.a-z0-9A-Z]*)""".r
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

  val ws = """[ \t]*""".r

  /**
   * End of Line
   */
  def eol = sys.props("line.separator")

  /**
   * End of Input
   */
  def eoi = """\z""".r

  /**
   * End Of Statement (statement separator)
   */
  def eos = token(";" | eol)

  def token[T](p: Parser[T]): Parser[T] = ws ~> p

  def seek[T](p: Parser[T]): Parser[T] = rep(eos) ~> p

  /**
   * 頂点束縛(Vertex Binding)
   */
  def binding: Parser[AstVertexBinding] = "$" ~> """[a-zA-Z0-9]+""".r ^^ AstVertexBinding

  /**
   * ラベルのパーサー
   */
  def label: Parser[AstLabel] = PAT_LABEL ^^ AstLabel

  /**
   * 頂点に付くラベルと変数の組み合わせ
   */
  def name: Parser[AstName] =
    (binding ~ opt("[" ~> label <~ "]")) ^^ {
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

  /**
   * 構文解析器における頂点とは，v(foo: bar)のような構文上正規の頂点のみです
   * Cell等もランタイムでは頂点ですが，ここでは頂点に含まれません
   */
  def vertex: Parser[AstVertex] = name ~ opt(attributes) ~ opt(edges) ^^ {
    case n ~ attrs ~ Some(es) => AstVertex(n, attrs, es)
    case n ~ attrs ~ None => AstVertex(n, attrs, Seq())
  }

  def cell: Parser[AstCell] = "{" ~> cellBody <~ "}"

  def cellBody: Parser[AstCell] = cellItemList ^^ {
    case exprs => AstCell(exprs)
  }

  /**
   * fin文
   */
  def cell_fin: Parser[AstFin] = "fin" ~> expr ^^ AstFin

  /**
   * Import文
   * TODO: TermParserをCellごとに生成して，Moduleレベルでfinが来たらエラーみたいな処理を入れる？
   */
  def cell_import: Parser[AstImport] =
    opt("from" ~> label) ~
      "import" ~ rep1sep(label, ",") ~
      opt("as" ~> label) ^^ {
      case frm ~ _ ~ imports ~ as => AstImport(frm, imports, as)
    }

  def cellItem: Parser[AstCellItem] = seek(cell_import | cell_fin | expr)

  def cellItemList: Parser[Seq[AstCellItem]] = rep(cellItem)

  /**
   * 二項演算子のオペランドになる項
   */
  def element: Parser[AstGraph] = "(" ~> expr <~ ")" | cell | vertex

  /**
   * 二項演算子
   */
  def binop: Parser[AstBinOp] = "[!@#%^&*+=|:<>/?.-]+".r ^^ {
    case exp => AstBinOp(exp)
  }

  /**
   * {@code expr}において，次の演算子と項の部分
   */
  def binopRight: Parser[(AstBinOp, AstGraph)] = binop ~ element ^^ {
    case op ~ ex => (op, ex)
  }

  /**
   * 頂点と演算子の列
   * foo -> {bar; hgoe -> fuga} -> foo * bar
   * foo, (->, {bar; hoge -> fuga}), (->, foo), (*, bar)
   */
  def expr: Parser[AstExpr] = "(" ~> expr <~ ")" | element ~ rep(binopRight) ^^ {
    case exp ~ followingExprs =>
      AstExpr(exp, followingExprs)
  }

  def apply(expr: String): Ast = {
    parseAll(cellBody, expr) match {
      case Success(gr, _) => new Ast(gr)
      case fail: NoSuccess => {
        throw new SyntaxError(s"${fail.toString} \nat line ${fail.next.pos.line} col ${fail.next.pos.column}")
      }
    }
  }
}

object TermParser {
  val default = new TermParser()
}
