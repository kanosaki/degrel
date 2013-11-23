package degrel.core

trait Label {
  def expr: String
}

object Label {
  val specials: Map[String, Label] =
    Map("*" -> WildcardLabel,
        "@" -> ReferenceLabel)
  val wildcard = WildcardLabel
  val reference = ReferenceLabel

  implicit def apply(expr: String): Label = {
    specials.get(expr) match {
      case None => BasicLabel(expr)
      case Some(s) => s
    }
  }
}

case class BasicLabel(expr: String) extends Label {

}

case object WildcardLabel extends Label {
  def expr: String = "*"
}

case object ReferenceLabel extends Label {
  def expr: String = "@"
}


