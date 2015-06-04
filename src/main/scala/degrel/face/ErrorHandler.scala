package degrel.face

import spray.routing.{Route, ExceptionHandler}

class ErrorHandler extends ExceptionHandler {
  override def isDefinedAt(x: Throwable): Boolean = ???

  override def apply(v1: Throwable): Route = ???
}
