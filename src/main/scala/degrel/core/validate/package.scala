package degrel.core

/**
 * `degrel.core`グラフを走査し，正当なデータになっているかテストします
 */
package object validate {
  def essential(target: Element, whole: Boolean = true): Seq[ValidationFailure] = {
    val ruleSet = Seq()
    Validator(ruleSet)(target, whole)
  }
}
