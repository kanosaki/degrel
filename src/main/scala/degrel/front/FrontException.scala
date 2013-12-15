package degrel.front

import degrel.DegrelException

class FrontException(val msg: String) extends DegrelException(msg) {

}

class SyntaxError(msg: String) extends FrontException(msg) {
}

class ResolveError(resolveTarget: String, msg: String) extends FrontException(msg) {
}
