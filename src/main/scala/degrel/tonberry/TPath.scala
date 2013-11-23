package degrel.tonberry

import degrel.core
case class TPath(expr: String) {
  def toQuery : Query[core.Element] = {
    throw new NotImplementedError()
  }
}
