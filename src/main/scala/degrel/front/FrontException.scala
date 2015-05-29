package degrel.front

import degrel.DefaultDegrelException

class FrontException(msg: String, cause: Throwable = null) extends DefaultDegrelException(msg, null) {

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
