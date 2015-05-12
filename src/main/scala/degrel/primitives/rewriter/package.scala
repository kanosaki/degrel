package degrel.primitives

import degrel.primitives.rewriter.cell.{Fin, SendMessage}
import degrel.primitives.rewriter.lang.{IntegerPredicate, If}
import degrel.primitives.rewriter.math.Plus

package object rewriter {
  val default = Seq(new Plus(), new SendMessage(), new Fin(), new If()) ++ IntegerPredicate.all
}
