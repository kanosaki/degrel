package degrel.builtins

import degrel.builtins.cell.SendMessage
import degrel.builtins.rewriter.math.Plus

package object rewriter {
  val default = Seq(new Plus(), new SendMessage())
}
