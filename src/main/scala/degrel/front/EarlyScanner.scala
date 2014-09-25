package degrel.front

import scala.util.parsing.combinator.RegexParsers

/**
 * 構文解析を行う前に，import等を読み取り構文解析器を生成します
 */
object EarlyScanner extends RegexParsers {
  def cellScanner: Parser[ParserContext] = {
    ???
  }

}
