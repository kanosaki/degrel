package degrel.front

case class BinOp(expr: String,
                 precedence: Int = 0,
                 associativity: OpAssoc = OpAssoc.Left) {

}

trait OpAssoc

object OpAssoc {

  case object Right extends OpAssoc

  case object Left extends OpAssoc

}
