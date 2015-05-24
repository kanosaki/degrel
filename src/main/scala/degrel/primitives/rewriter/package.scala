package degrel.primitives

import degrel.primitives.rewriter.cell.{Fin, SendMessage}
import degrel.primitives.rewriter.io.Println
import degrel.primitives.rewriter.lang.{BoolOperations, If, IntegerPredicate}

package object rewriter {
  val default = math.default ++
    BoolOperations.all ++
    IntegerPredicate.all ++
    Seq(new SendMessage(),
        new If(),
        new Println(),
        new Fin())
}
