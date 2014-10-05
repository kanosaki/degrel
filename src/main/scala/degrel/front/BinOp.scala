package degrel.front

/**
 * 二項演算子を定義します．演算子の構文上の性質のみの定義で，意味は定義されません．
 * @param expr 演算子の文字列
 * @param precedence 演算子の優先順位．数値が大きいほど優先されます．デフォルトでは0です
 * @param associativity 演算子の結合規則．デフォルトでは左結合です
 */
case class BinOp(expr: String,
                 precedence: Int = 0,
                 associativity: OpAssoc = OpAssoc.Left) extends Ordered[BinOp] with AstCellItem {
  /**
   * 演算子優先順序を定義します．優先順位が高いほど優先され，右結合と左結合の演算子がある場合は左結合が優先されます
   */
  override def compare(that: BinOp): Int = {
    if (this.precedence != that.precedence) {
      that.precedence - this.precedence
    } else {
      if (this.associativity != that.associativity) {
        this.associativity.compare(that.associativity)
      } else {
        0
      }
    }
  }
}

object BinOp {
  // デフォルトの演算子優先順位．ほぼJavaに準拠
  // 上ほど優先のもので，下ほど優先度が低い
  val DOT = BinOp(".", 5)
  val EXP = BinOp("**", 3)
  val MUL = BinOp("*", 2)
  val DIV = BinOp("/", 2)
  val MOD = BinOp("%", 2)
  val ADD = BinOp("+", 1)
  val SUB = BinOp("-", 1)
  // デフォルトではここに演算子の優先順位
  val BITSHIFT_RIGHT = BinOp(">>", -1)
  val BITSHIFT_LEFT = BinOp("<<", -1)
  val LESS = BinOp("<", -2)
  val GREATER = BinOp(">", -2)
  val LESS_EQUAL = BinOp("<=", -2)
  val GREATER_EQUAL = BinOp(">=", -2)
  val EQUALS = BinOp("==", -3)
  val NOT_EQUALS = BinOp("!=", -3)
  val BIT_AND = BinOp("&", -4)
  val BIT_OR = BinOp("|", -4)
  val BIT_XOR = BinOp("^", -4)
  val BOOL_AND = BinOp("&&", -5)
  val BOOL_OR = BinOp("||", -6)
  // メッセージ送信
  val MSG_SEND = BinOp("!", -7, OpAssoc.Right)
  // メッセージ送信 & 受信
  val MSG_CALL = BinOp("?", -7, OpAssoc.Right)
  val RULE = BinOp("->", -10, OpAssoc.Right)
  val BIND = BinOp("=", -20, OpAssoc.Right)

  /**
   * 標準の演算子定義．
   */
  val builtins = Seq(
    EXP, MUL, DIV, MOD, ADD, SUB,
    BITSHIFT_RIGHT, BITSHIFT_LEFT,
    LESS, GREATER, LESS_EQUAL, GREATER_EQUAL,
    EQUALS, NOT_EQUALS,
    BIT_AND, BIT_OR, BIT_XOR,
    BOOL_AND, BOOL_OR,
    MSG_SEND, MSG_CALL,
    RULE, BIND
  )

}

/**
 * 演算子の結合規則．
 */
trait OpAssoc extends Ordered[OpAssoc] {
}

object OpAssoc {

  /**
   * 右結合
   */
  case object Right extends OpAssoc {
    override def compare(that: OpAssoc) = {
      that match {
        case Right => 0
        case Left => 1
      }
    }
  }

  /**
   * 左結合
   */
  case object Left extends OpAssoc {
    override def compare(that: OpAssoc) = {
      that match {
        case Right => -1
        case Left => 0
      }
    }
  }

}
