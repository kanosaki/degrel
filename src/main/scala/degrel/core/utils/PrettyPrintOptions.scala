package degrel.core.utils

case class PrettyPrintOptions(showAllId: Boolean = true,
                              multiLine: Boolean = false,
                              indentItem: String = "  ") {

}

object PrettyPrintOptions {
  final val default = PrettyPrintOptions()
}

