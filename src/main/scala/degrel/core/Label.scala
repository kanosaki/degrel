package degrel.core

trait Label {

}

object Label {
  val specials : Map[String, Label] = Map(
    "*" -> WildcardLabel,
    "@" -> ReferenceLabel
  )
  def apply(expr: String) : Label = {
    specials.get(expr) match {
      case None => BasicLabel(expr)
      case Some(s) => s
    }
  }
}

case class BasicLabel(expr: String) extends Label {

}

case object WildcardLabel extends Label {

}

case object ReferenceLabel extends Label {

}


