package degrel.dgspec.fragments

import degrel.dgspec.{Dgspec, DgspecContext, Fragment, NextFragment}

class AssertFragment(stateExpr: String, conditionExpr: String) extends Fragment {
  override def execute(spec: Dgspec, context: DgspecContext): NextFragment = ???
}
