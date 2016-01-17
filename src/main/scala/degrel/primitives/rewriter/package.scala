package degrel.primitives

import degrel.primitives.rewriter.cell.{Fin, SendMessage}
import degrel.primitives.rewriter.io.Println
import degrel.primitives.rewriter.lang.{BoolOperations, If, IntegerPredicate}
import degrel.primitives.rewriter.misc.Sleep

package object rewriter {
  val default = math.default ++
    BoolOperations.all ++
    IntegerPredicate.all ++
    Seq(new SendMessage(),
        new If(),
        new Sleep(),
        new Println(),
        new Fin())
}
