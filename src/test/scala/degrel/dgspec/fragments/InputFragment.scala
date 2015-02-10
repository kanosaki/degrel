package degrel.dgspec.fragments

import degrel.dgspec.{NextFragment, DgspecContext, Dgspec, Fragment}

class InputFragment(val initState: String) extends Fragment {
  override def execute(spec: Dgspec, context: DgspecContext): NextFragment = {
    NextFragment.Continue
  }
}
