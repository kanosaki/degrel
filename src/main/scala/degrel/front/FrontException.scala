package degrel.front

class FrontException(msg: String) extends Exception(msg) {

}

class SyntaxError(msg: String) extends FrontException(msg) {
}

class ResolveError(resolveTarget: String, msg: String) extends FrontException(msg) {
}
