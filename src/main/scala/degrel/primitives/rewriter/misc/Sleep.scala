package degrel.primitives.rewriter.misc

import degrel.core.{Label, Vertex}
import degrel.engine.rewriting.{RewriteResult, Rewriter, RewritingTarget}
import degrel.utils.PrettyPrintOptions

class Sleep extends Rewriter {
  val sleepLabel = Label("sleep")

  override def rewrite(rc: RewritingTarget): RewriteResult = {
    val target = rc.target
    if (target.label == this.sleepLabel) {
      target.thru(0).headOption match {
        case Some(v) => {
          try {
            val sleepMs = v.label.expr.toInt
            RewriteResult.IO(driver => {
              logger.info(s"Sleeping $sleepMs ms")
              Thread.sleep(sleepMs)
              driver.writeVertex(target, Vertex.vNil)
            })
          } catch {
            case _: NumberFormatException => nop
          }
        }
        case _ => nop
      }
    } else {
      nop
    }
  }


  override def pattern: Vertex = parse("sleep(ms: _)")

  override def pp(implicit opt: PrettyPrintOptions): String = "<Builtin-rule sleep>"
}
