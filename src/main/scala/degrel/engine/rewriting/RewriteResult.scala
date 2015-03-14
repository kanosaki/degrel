package degrel.engine.rewriting

class RewriteResult(val done: Boolean,
                    val continuation: Continuation = Continuation.Empty) {
}

object RewriteResult {
  def apply(done: Boolean) = {
    new RewriteResult(done)
  }

  def apply(done: Boolean, cont: Continuation) = {
    new RewriteResult(done, cont)
  }
}

