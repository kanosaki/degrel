package degrel.front

object Tokens {
}

object Keywords {

}

object BinOp {
  val rule = "->"
}

object SpecialLabel {

  object Edge {
    val lhs = "_lhs"
    val rhs = "_rhs"
    val ref = "_ref"
  }

  object Vertex {
    val reference = "@"
    val wildcard = "*"
    val rule = BinOp.rule
  }

}

