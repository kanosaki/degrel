package degrel.engine.rewriting.matching

trait MatchingContext {

}

class InitialContext extends MatchingContext {

}

object MatchingContext {
  def empty: MatchingContext = {
    new InitialContext()
  }
}
