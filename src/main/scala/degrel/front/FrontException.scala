package degrel.front

import degrel.DegrelException

class FrontException(val msg: String) extends DegrelException(msg) {

}

class SyntaxError(msg: String) extends FrontException(msg) {
}

class ResolveError(resolveTarget: String, msg: String) extends FrontException(msg) {
}

/**
 * プログラム上の制約違反
 * @param msg
 */
class CodeException(msg: String) extends FrontException(msg) {

}
