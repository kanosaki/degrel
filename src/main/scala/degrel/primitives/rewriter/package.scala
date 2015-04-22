package degrel.primitives

import degrel.primitives.cell.SendMessage
import degrel.primitives.rewriter.math.Plus

package object rewriter {
  val default = Seq(new Plus(), new SendMessage())
}
