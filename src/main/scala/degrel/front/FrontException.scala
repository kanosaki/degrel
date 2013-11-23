package degrel.front

class FrontException extends Exception {

}

class SyntaxError(msg: String) extends FrontException {
  override def toString = {
    msg
  }
}

class ResolveError(resolveTarget: String) extends FrontException {
}
