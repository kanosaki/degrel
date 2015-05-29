package degrel.front

import org.parboiled2._

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

  val KEYCHAR = CharPredicate(":;=#@\u21d2\u2190")
  val KEYCHAR2 = CharPredicate("-:%")


  def hexNum: Rule1[String] = rule {
    atomic("0x" ~ capture(HEXDIGIT.+))
  }

  def decNum: Rule1[String] = rule {
    atomic(capture('-'.? ~ DIGIT.+))
  }

  def newline = rule(quiet('\r'.? ~ '\n'))

  def opChar = rule(atomic(OPCHAR | test(isMathOrOtherSymbol(this.cursorChar)) ~ ANY))

  private def GeneralAlphaNum = rule(test(this.cursorChar.isLetter | this.cursorChar.isDigit) ~ ANY)

  def alphaNum = rule {
    ALPHANUM | GeneralAlphaNum
  }

  def lowerAlphaNum = rule(atomic(LOWER | test(this.cursorChar.isLower | this.cursorChar.isDigit) ~ ANY))

  def lowerAlpha = rule(atomic(LOWER | test(this.cursorChar.isLower) ~ ANY))

  def upperAlpha = rule(atomic(UPPER | test(this.cursorChar.isUpper) ~ ANY))

  private def blockComment: Rule0 = rule("/*" ~ (blockComment | !"*/" ~ ANY).* ~ "*/")

  private def comment: Rule0 = rule(blockComment | "#" ~ (!newline ~ ANY).*)

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

  def integer: Rule1[AstIntegerVertex] = rule {
    hexNum ~> (AstIntegerVertex(_: String, 16)) |
      decNum ~> (AstIntegerVertex(_: String, 10))
  }

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

  def variable = rule {
    capture(upperAlpha ~ ('_' | alphaNum).*) ~ ws ~> AstBinding
  }

  def binding = rule {
    '@' ~ ws ~ variable ~ ws
  }

  def label = rule {
    capture((lowerAlphaNum | '_') ~ (alphaNum | '.' | '_').*) ~ ws ~> AstLabel
  }

  def quotedLabel = rule {
    ''' ~ capture((!''' ~ ANY).*) ~ ''' ~ ws ~> AstLabel
  }

  def vertexHead = {
    val labels = rule(quotedLabel | label)
    rule {
      labels ~ binding.? ~> ((l, bind) => AstName(Some(l), bind)) |
        binding ~> (bind => AstName(Some(AstLabel("_")), Some(bind))) |
        variable ~> (v => AstName(None, Some(v)))
    }
  }

  def binop: Rule1[AstBinOp] = rule {
    capture(opChar.+) ~> (AstBinOp(_: String))
  }

  def binopRight: Rule1[(AstBinOp, AstVertex)] = rule {
    binop ~ wl ~ element ~> ((b: AstBinOp, v: AstVertex) => (b, v))
  }

  def expression: Rule1[AstVertex] = rule {
    element ~ binopRight.* ~> {
      AstLinerExpr(_: AstVertex, _: Seq[(AstBinOp, AstVertex)]).toTree
    }
  }

  def normalEdge: Rule1[AstAbbrEdge] = rule {
    label ~ wl ~ ':' ~ wl ~ expression ~> ((l, v) => AstAbbrEdge(Some(l), v)) |
      (label ~ wl ~ ':' ~ wl).? ~ element ~> AstAbbrEdge
  }

  def othersEdge: Rule1[AstOthersEdges] = rule {
    '_' ~ wl ~ ':' ~ wl ~ (
      binding ~> (AstOthersEdges(_: AstBinding, isDeclare = true)) |
        variable ~> (AstOthersEdges(_: AstBinding, isDeclare = false)))
  }

  def edgeSeparator: Rule0 = rule {
    ws ~ ',' ~ wl
  }

  def edge: Rule1[AstEdgeElement] = rule(othersEdge | normalEdge)

  def edgesList: Rule1[Seq[AstEdgeElement]] = rule {
    edge.*(edgeSeparator)
  }

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

  def edges = rule {
    ('(' ~ wl ~ edgesList ~ wl ~ ')' | edgesList) ~> verifyEdgeElements _
  }

  def functor: Rule1[AstFunctor] = rule {
    vertexHead ~ edges.? ~> ((_: AstName, _: Option[AstEdges]) match {
      case (h, Some(es)) => AstFunctor(h, None, es)
      case (h, None) => AstFunctor(h, None, AstEdges(Seq(), Seq()))
    })
  }

  def element: Rule1[AstVertex] = rule {
    ws ~ ('(' ~ expression ~ ')' | cell | valueVertex | functor) ~ ws
  }

  def cellItem: Rule1[AstCellItem] = rule {
    wl ~ expression ~ semis ~ wl
  }

  def cell: Rule1[AstCell] = rule {
    '{' ~ wl ~ cellItem.*  ~ '}' ~ wl ~> AstCell
  }

  def valueVertex: Rule1[AstVertex] = rule {
    string | float | integer
  }

  def allAsExpression = rule {
    wl ~ expression ~ wl~ EOI
  }

  def allAsCell = rule {
    wl ~ cell ~ wl ~ EOI
  }
}

