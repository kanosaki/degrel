package degrel.rewriting

trait MatchingContext {

}

class InitialContext extends MatchingContext {

}

object MatchingContext {
  def empty: MatchingContext = {
    new InitialContext()
  }
}
