package degrel.front

import scala.language.higherKinds
import scala.util.parsing.combinator.JavaTokenParsers

class TermParser(val parsercontext: ParserContext = ParserContext.default) extends JavaTokenParsers {
  implicit val context = parsercontext

  /**
   * 頂点のラベルとして使えるものの正規表現
   */
  val PAT_LABEL = """([_a-z0-9][_.a-z0-9A-Z]*)""".r
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

  val PAT_BINOP = """[\-\.!#$%^&*+=|:<>/?]+""".r

  val PAT_FULL_LABEL = """[_\-\.!#$%^&*+=|:<>/?A-Za-z0-9]+""".r

  val PAT_BINDING = """[A-Z0-9][a-zA-Z0-9_]*""".r

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
  def binding: Parser[AstBinding] = PAT_BINDING ^^ AstBinding

  def bindingDeclare: Parser[AstBinding] = "@" ~> binding

  /**
   * ラベルのパーサー
   */
  def label: Parser[AstLabel] = PAT_LABEL ^^ AstLabel

  def fullLabel: Parser[AstLabel] = "'" ~> PAT_FULL_LABEL <~ "'" ^^ AstLabel | label

  /**
   * 頂点に付くラベルと変数の組み合わせ
   */
  def name: Parser[AstName] =
    label ~ opt(bindingDeclare) ^^ {
      case lbl ~ b => AstName(Some(lbl), b)
    } | fullLabel ~ opt(bindingDeclare) ^^ {
      case lbl ~ b => AstName(Some(lbl), b)
    } | bindingDeclare ^^ {
      case bind => AstName(Some(AstLabel("_")), Some(bind))
    } | binding ^^ {
        case b => AstName(None, Some(b))
      }

  def othersEdge: Parser[AstEdgeElement] = ("_" ~ ":") ~> (
    binding ^^ {
      AstOthersEdges(_, isDeclare = false)
    } |
      bindingDeclare ^^ {
        AstOthersEdges(_, isDeclare = true)
      }
    )

  def normalEdge: Parser[AstEdgeElement] = opt(label <~ ":") ~ expr ^^ {
    case n ~ v => AstAbbrEdge(n, v)
  }

  /**
   * 接続のパーサー
   * MEMO:
   * `othersEdge`は`normalEdge`としても有効なので，`othersEdge`を優先します
   */
  def edge: Parser[AstEdgeElement] = othersEdge | normalEdge

  /**
   * 頂点の持つ接続の集合パーサー
   */
  def edges: Parser[AstEdges] = "(" ~> repsep(edge, ",") <~ ")" ^^ (abbrEdges => {
    // 最後の要素がOthersEdgeであるかどうかを確認します
    val (plainEs, othersE) = abbrEdges.last match {
      case oe: AstOthersEdges => (abbrEdges.dropRight(1), Some(oe))
      case _ => (abbrEdges, None)
    }
    // 非省略接続の後の省略接続は許可されないので，それを確認します
    plainEs.foldLeft('hasAbbr) {
      case ('hasAbbr, AstAbbrEdge(Some(_), _)) => 'noAbbr
      case ('hasAbbr, AstAbbrEdge(None, _)) => 'hasAbbr
      case ('noAbbr, AstAbbrEdge(Some(_), _)) => 'noAbbr
      case ('noAbbr, AstAbbrEdge(None, _)) =>
        throw new SyntaxError("Cannot use edge label abbreviation after non-abbreviated edge.")
    }
    // 省略形の接続を復元します
    val fullPlainEs = plainEs.zipWithIndex.map {
      case (item: AstAbbrEdge, index) => item.toFullForm(index)
      case (item: AstEdge, index) => item
    }
    AstEdges(fullPlainEs, othersE)
  })


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
  def functor: Parser[AstFunctor] = name ~ opt(attributes) ~ opt(edges) ^^ {
    case n ~ attrs ~ Some(es) => AstFunctor(n, attrs, es)
    case n ~ attrs ~ None => AstFunctor(n, attrs, AstEdges(Seq(), None))
  }

  def cell: Parser[AstCell] = "{" ~> {
    val nextCtx = new ParserContext(context)
    // this.type#Parser[AstCell]をコンパイラが要求してくるのでキャスト
    // Scalaパーサーの仕様
    val nextParser = new TermParser(nextCtx).asInstanceOf[this.type]
    nextParser.cellBody
  } <~ "}"

  def cellBody: Parser[AstCell] = cellItemList ^^ {
    case exprs => AstCell(exprs)
  }

  /**
   * fin文
   */
  def cell_fin: Parser[AstFin] = "fin" ~> expr ^^ {
    case x => AstFin(x)
  }

  // NOTE: 副作用あり(this method has side effects)
  /**
   * 二項演算子定義．定義と同時にParserContextへ定義された演算子を追加します
   * @return
   */
  def cell_defop: Parser[BinOp] =
    "defop" ~> PAT_BINOP ~ opt(wholeNumber) ~ opt("right" | "left") ^^ {
      case op ~ precedenceOpt ~ associativityOpt => {
        val precedence = precedenceOpt match {
          case Some(p) => p.toInt
          case None => 0 // Default precedence
        }

        val associativity = associativityOpt match {
          case Some("right") => OpAssoc.Right
          case Some("left") => OpAssoc.Left
          case Some(_) => throw new RuntimeException("Never here")
          case None => OpAssoc.Left
        }
        val bop = BinOp(op, precedence, associativity)
        context.addOperator(bop)
        bop
      }
    }

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

  def cellItem: Parser[AstCellItem] = seek(cell_import | cell_defop | cell_fin | expr)

  def cellItemList: Parser[Seq[AstCellItem]] = rep(cellItem)

  /**
   * 二項演算子のオペランドになる項
   */
  def element: Parser[AstVertex] = "(" ~> expr <~ ")" | cell | functor

  /**
   * 二項演算子
   */
  def binop: Parser[AstBinOp] = PAT_BINOP ^^ {
    case exp => AstBinOp(exp)
  }

  /**
   * {@code expr}において，次の演算子と項の部分
   */
  def binopRight: Parser[(AstBinOp, AstVertex)] = binop ~ element ^^ {
    case op ~ ex => (op, ex)
  }

  /**
   * 頂点と演算子の列
   * foo -> {bar; hgoe -> fuga} -> foo * bar
   * foo, (->, {bar; hoge -> fuga}), (->, foo), (*, bar)
   */
  def expr: Parser[AstVertex] = element ~ rep(binopRight) ^^ {
    case exp ~ followingExprs =>
      AstLinerExpr(exp, followingExprs).toTree
  }

  def parseExpr(str: String): AstVertex = {
    parseAll(expr, str) match {
      case Success(e, _) => e
      case fail: NoSuccess => {
        throw new SyntaxError(s"${fail.toString} \nat line ${fail.next.pos.line} col ${fail.next.pos.column}")
      }
    }
  }

  def parseCell(str: String): AstCell = {
    parseAll(cell, str) match {
      case Success(gr, _) => gr
      case fail: NoSuccess => {
        throw new SyntaxError(s"${fail.toString} \nat line ${fail.next.pos.line} col ${fail.next.pos.column}")
      }
    }
  }
}

object TermParser {
  val default = new TermParser()

  def parseExpr(s: String) = default.parseExpr(s)

  def parseCell(s: String) = default.parseCell(s)

}
