package degrel.front

import org.parboiled2._
import shapeless.{::, HNil}

class ParboiledParser(val input: ParserInput) extends Parser {
  implicit val parserContext = new ParserContext()

  val DIGIT = CharPredicate.Digit

  val HEXDIGIT = CharPredicate.HexDigit

  val `Ee` = CharPredicate("Ee")

  val `Ll` = CharPredicate("Ll")

  val `+-` = CharPredicate("+-")

  val DQLF = CharPredicate("\"\n")
  val DQBS = CharPredicate("\"\\")

  val `FfDd` = CharPredicate("FfDd")

  val ESCAPE = CharPredicate( """btnfr'\"""")

  val WSCHAR = CharPredicate(" \t")

  val OPCHAR = CharPredicate( """!$%&*+-/<=>?\^|~""")

  val LOWER = CharPredicate.LowerAlpha

  val UPPER = CharPredicate.UpperAlpha

  val ALPHA = CharPredicate.Alpha

  val ALPHANUM = CharPredicate.AlphaNum

  /**
   * 16進数リテラル
   * TODO: 負の数ってどうするの
   */
  def hexNum: Rule1[String] = rule {
    atomic("0x" ~ capture(HEXDIGIT.+))
  }

  /**
   * 10進数整数
   */
  def decNum: Rule1[String] = rule {
    atomic(capture('-'.? ~ DIGIT.+))
  }

  def newline = rule(quiet('\r'.? ~ '\n'))

  def opChar = rule(atomic(OPCHAR | test(isMathOrOtherSymbol(this.cursorChar)) ~ ANY))

  private def GeneralAlphaNum = rule(test(this.cursorChar.isLetter | this.cursorChar.isDigit) ~ ANY)

  def alphaNum = rule {
    ALPHANUM | GeneralAlphaNum
  }

  def lowerAlphaNum = rule(atomic(LOWER | DIGIT | test(this.cursorChar.isLower | this.cursorChar.isDigit) ~ ANY))

  def lowerAlpha = rule(atomic(LOWER | test(this.cursorChar.isLower) ~ ANY))

  def upperAlpha = rule(atomic(UPPER | test(this.cursorChar.isUpper) ~ ANY))

  private def blockComment: Rule0 = rule("/*" ~ (blockComment | !"*/" ~ ANY).* ~ "*/")

  private def comment: Rule0 = rule(blockComment | "//" ~ (!newline ~ ANY).*)

  private def commentLine = rule(quiet(WSCHAR.* ~ comment ~ WSCHAR.* ~ newline))

  /**
   * Whitespace, including newlines. This is the default for most things.
   */
  def wl = rule(quiet((WSCHAR | comment | newline).*))

  def lineEnd = rule(quiet(wl ~ newline))

  /**
   * Whitespace, excluding newlines.
   * Only really useful in e.g. {} blocks, where we want to avoid
   * capturing newlines so semicolon-inference works
   */
  def ws = rule(quiet(WSCHAR | comment).*)

  def semi = rule(wl ~ ';' | ws ~ newline.+)

  def semis = rule(semi.*)

  def notNewline: Rule0 = rule(&(ws ~ !newline))

  def oneNLMax: Rule0 = rule(quiet(ws ~ newline.? ~ commentLine.* ~ notNewline))

  /**
   * 整数
   */
  def integer: Rule1[AstIntegerVertex] = rule {
    hexNum ~> (AstIntegerVertex(_: String, 16)) |
      decNum ~> (AstIntegerVertex(_: String, 10))
  }

  /**
   * 浮動小数点
   */
  def float: Rule1[AstFloatVertex] = {
    val Exp = rule(`Ee` ~ `+-`.? ~ DIGIT.+)
    val Decimals = rule('.' ~ DIGIT.+ ~ Exp.? ~ `FfDd`.?)
    rule(atomic(capture(Decimals | DIGIT.+ ~ (Decimals | Exp ~ `FfDd`.? | `FfDd`))) ~> AstFloatVertex)
  }

  def mapEscapeChar(c: String) = c match {
    case "b" => '\b'
    case "t" => '\t'
    case "n" => '\n'
    case "f" => '\f'
    case "r" => '\r'
    case "'" => '''
    case "\"" => '"'
  }

  // TODO: use StringBuilding here
  /**
   * 文字列リテラル
   */
  def string: Rule1[AstStringVertex] = {
    def stringToken: Rule1[Char] = rule {
      '\\' ~ capture(ESCAPE) ~> (mapEscapeChar(_: String)) |
        &(!'"') ~ capture(ANY) ~> ((_: String).charAt(0))
    }
    rule(atomic('"' ~ stringToken.* ~ '"') ~> ((cs: Seq[Char]) => AstStringVertex(cs.mkString, trimQuotes = false)))
  }

  private def isMathOrOtherSymbol(c: Char) =
    Character.getType(c) match {
      case Character.OTHER_SYMBOL | Character.MATH_SYMBOL => true
      case _ => false
    }

  private def isPrintableChar(c: Char): Boolean =
    !Character.isISOControl(c) &&
      !Character.isSurrogate(c) && {
      val block = Character.UnicodeBlock.of(c)
      block != null && block != Character.UnicodeBlock.SPECIALS
    }

  /**
   * 変数参照
   */
  def variable = rule {
    capture(upperAlpha ~ ('_' | alphaNum).*) ~ ws ~> AstBinding
  }

  /**
   * 変数宣言
   */
  def binding = rule {
    '@' ~ ws ~ variable
  }

  /**
   * ラベル
   */
  def label = rule {
    capture((lowerAlphaNum | '_') ~ (alphaNum | '.' | '_').*) ~ ws ~> AstLabel
  }

  /**
   * クオートラベル
   * 任意の文字をラベルとして使える
   */
  def quotedLabel = rule {
    ''' ~ capture((!''' ~ ANY).*) ~ ''' ~ ws ~> AstLabel
  }

  def labels = rule(quotedLabel | label)

  /**
   * 頂点のHEAD部．
   * @note データの`VertexHeader`とは別物で，構文上でのHEAD部
   */
  def vertexHead = rule {
    labels ~ binding.? ~> ((l, bind) => AstName(Some(l), bind)) |
      binding ~> (bind => AstName(Some(AstLabel("_")), Some(bind))) |
      variable ~> (v => AstName(None, Some(v)))
  }

  /**
   * 二項演算子
   */
  def binop: Rule1[AstBinOp] = rule {
    capture(opChar.+) ~> (AstBinOp(_: String))
  }

  /**
   * 連続した二項演算子を構文解析するときの
   * 二項演算子とそれに続く部分
   */
  def binopRight: Rule1[(AstBinOp, AstVertex)] = rule {
    binop ~ wl ~ element ~> ((b: AstBinOp, v: AstVertex) => (b, v))
  }

  /**
   * 式
   */
  def expression: Rule1[AstVertex] = rule {
    element ~ binopRight.* ~> {
      AstLinerExpr(_: AstVertex, _: Seq[(AstBinOp, AstVertex)]).toTree
    }
  }

  /**
   * 通常の接続
   */
  def normalEdge(strong: Boolean): Rule1[AstAbbrEdge] = strong match {
    case true => rule {
      (label ~ wl ~ ':' ~ wl).? ~ element ~> AstAbbrEdge
    }
    case false => rule {
      (label ~ wl ~ ':' ~ wl).? ~ expression ~> AstAbbrEdge
    }
  }


  /**
   * Others接続
   */
  def othersEdge: Rule1[AstOthersEdges] = rule {
    '_' ~ wl ~ ':' ~ wl ~ (
      binding ~> (AstOthersEdges(_: AstBinding, isDeclare = true)) |
        variable ~> (AstOthersEdges(_: AstBinding, isDeclare = false)))
  }

  /**
   * エッジの区切り．現在 行末コロンのみ可能です．
   */
  def edgeSeparator: Rule0 = rule {
    ws ~ ',' ~ wl
  }

  /**
   * 接続のパーサー
   * MEMO:
   * `othersEdge`は`normalEdge`としても有効なので，`othersEdge`を優先します
   */
  def edge(strong: Boolean): Rule1[AstEdgeElement] = rule(othersEdge | normalEdge(strong))

  /**
   * 接続のリスト
   */
  def edgesList(strong: Boolean): Rule1[Seq[AstEdgeElement]] = rule {
    edge(strong).*(edgeSeparator)
  }

  /**
   * 接続は属性を省略可能であるため，省略の方法が正規であるか確認し，
   * 正規である場合は省略された部分を復元します．
   *
   * 具体的には`OthersEdge`は最後に固めておかなければならず，省略接続は
   * 非省略接続の後ろに現れることが出来ません．つまり接続は
   * `AbbreviatedEdge.* ~ NormalEdge.* ~ OthersEdge.*`の形をしている必要があります
   */
  def verifyEdgeElements(abbrEdges: Seq[AstEdgeElement]): AstEdges = {
    if (abbrEdges.isEmpty) return AstEdges(Seq(), Seq())
    // 最後の要素がOthersEdgeであるかどうかを確認します
    val (plainEs, othersEs) = abbrEdges.span(!_.isInstanceOf[AstOthersEdges])

    // spanの定義より，plainEsの中にothersEsが入っていることはないが
    // othersEsに通常の接続が混じっている場合はエラー
    if (othersEs.exists(!_.isInstanceOf[AstOthersEdges])) {
      throw new SyntaxError("Cannot use plain edge after others edge.")
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
    AstEdges(fullPlainEs, othersEs.map(_.asInstanceOf[AstOthersEdges]))
  }

  def edges: Rule1[AstEdges] = rule {
    ('(' ~ wl ~ edgesList(false) ~ wl ~ ')' | edgesList(true)) ~> verifyEdgeElements _
  }

  def functor: Rule1[AstFunctor] = rule {
    vertexHead ~ edges.? ~> ((_: AstName, _: Option[AstEdges]) match {
      case (h, Some(es)) => AstFunctor(h, None, es)
      case (h, None) => AstFunctor(h, None, AstEdges(Seq(), Seq()))
    })
  }

  /**
   * 二項演算子の項
   */
  def element: Rule1[AstVertex] = rule {
    ws ~ ('(' ~ expression ~ ')' | cell | valueVertex | functor) ~ ws
  }

  def cellEdge: Rule1[AstCellEdge] = rule {
    labels ~ ':' ~ expression ~> AstCellEdge
  }

  def cellPargma: Rule1[AstCellPragma] = rule {
    "#" ~ ws ~ edges ~> AstCellPragma
  }

  /**
   * Cellの要素
   */
  def cellItem: Rule1[AstCellItem] = rule {
    cellPargma | cellEdge | expression
  }

  def cellBody: Rule1[AstCell] = rule {
    (wl ~ cellItem ~ semis).* ~> ((items: Seq[AstCellItem]) => AstCell(items.toVector))
  }

  def cell: Rule1[AstCell] = rule {
    '{' ~ wl ~ cellBody ~ wl ~ '}'
  }

  /**
   * リテラル頂点(`ValueVertex`)
   */
  def valueVertex: Rule1[AstVertex] = rule {
    string | float | integer
  }

  /**
   * 入力すべてを`expression`として扱います
   */
  def allAsExpression = rule {
    wl ~ expression ~ wl ~ EOI
  }

  /**
   * 入力すべてを`cell`として扱います
   */
  def allAsCell = rule {
    wl ~ cellBody ~ wl ~ EOI
  }
}

