package degrel.utils

case class PrettyPrintOptions(showAllId: Boolean = false,
                              multiLine: Boolean = false,
                              indentItem: String = "  ") {

}

object PrettyPrintOptions {
  final val default = PrettyPrintOptions()
}

