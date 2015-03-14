package degrel.engine.rewriting

import degrel.core.Rule

trait Continuation {

}

object Continuation {

  case class HasNext(rule: Rule, binding: Binding) extends Continuation {

  }

  case object Empty extends Continuation

}
