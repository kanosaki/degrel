package degrel.dgspec

/**
 * `DgspecContext`に対してVisitorパターンで
 * specの実行を行います
 */
trait Fragment {
  def execute(spec: Dgspec, context: DgspecContext): NextFragment
}

trait NextFragment {

}

object NextFragment {

  case object Continue extends NextFragment

  case class Next(fragment: Fragment) extends NextFragment

  case class Abort(msg: String) extends NextFragment

}
