package degrel.engine.rewriting

import degrel.core.Rule

/**
 * 継続を表します．
 */
trait Continuation {

}

object Continuation {

  /**
   * 継続が存在し，次に実行するべき`Rule`と，以前の`Binding`を保持します
   */
  case class Continue(rule: Rule, binding: Binding) extends Continuation {

  }

  /**
   * 継続無し
   */
  case object Empty extends Continuation

}
