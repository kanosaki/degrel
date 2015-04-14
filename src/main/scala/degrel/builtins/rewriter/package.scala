package degrel.builtins

import degrel.builtins.rewriter.math.Plus

package object rewriter {
  val default = Seq(new Plus())
}
