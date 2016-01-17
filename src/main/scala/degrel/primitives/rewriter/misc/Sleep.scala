package degrel.primitives.rewriter.misc

import degrel.core.{Label, Vertex}
import degrel.engine.rewriting.{RewriteResult, Rewriter, RewritingTarget}
import degrel.utils.PrettyPrintOptions

class Sleep extends Rewriter {
  val sleepLabel = Label("sleep")

  override def rewrite(rc: RewritingTarget): RewriteResult = {
    val target = rc.target
    if (target.label == this.sleepLabel) {
      (for {
        sleepMsV <- target.thru(0).headOption
        sleepMs <- sleepMsV.getValue[Int]
      } yield sleepMs) match {
        case Some(sleepMs) => {
          RewriteResult.IO(driver => {
            logger.info(s"Sleeping $sleepMs ms")
            Thread.sleep(sleepMs)
            driver.writeVertex(target, Vertex.vNil)
          })
        }
        case _ => {
          nop
        }
      }
    } else {
      nop
    }
  }


  override def pattern: Vertex = parse("sleep(ms: _)")

  override def pp(implicit opt: PrettyPrintOptions): String = "<Builtin-rule sleep>"
}
