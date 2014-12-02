package degrel.core

case class Name(names: List[String]) {
  def this(expr: String) = {
    this(expr.split(Name.SEPARATOR).toList)
  }

  override def toString: String = {
    s"Namespace(${names.mkString(this.separator)})"
  }

  def separator: String = Name.SEPARATOR
}

object Name {
  val SEPARATOR = "."
}
