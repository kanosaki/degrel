package degrel.core.validate

import degrel.core.Element

trait ValidationRules {
  type CheckFunc = PartialFunction[Element, Option[ValidationFailure]]

  def validate(container: Validator, e: Element): Iterable[ValidationFailure] = {
    this.rules.filter(_.isDefinedAt(e)).map(_(e)).collect {
      case Some(v) => v
    }
  }

  def rules: Iterable[CheckFunc]
}
