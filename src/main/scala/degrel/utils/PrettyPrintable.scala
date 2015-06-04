package degrel.utils

trait PrettyPrintable {
  def pp(implicit opt: PrettyPrintOptions = PrettyPrintOptions.default): String
}
