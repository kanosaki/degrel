package degrel.front

import degrel.core.{SpecialLabels => SL}

object Tokens {
}

object Keywords {

}

object SpecialLabel {

  object Edge {
    val tag = SL.E_TAG
    val lhs = SL.E_LHS
    val rhs = SL.E_RHS
    val ref = SL.E_REFERENCE_TARGET
  }

  object Vertex {
    val reference = SL.V_REFERENCE
    val wildcard = SL.V_WILDCARD
    val rule = SL.V_RULE
    val cell = SL.V_CELL
  }

}

