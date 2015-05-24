package degrel.primitives.rewriter.lang

import degrel.core.{Label, Vertex}
import degrel.front.BinOp
import degrel.primitives.rewriter.BinOpRewriter

object BoolOperations {
  val all = Seq(new Eq(), new Neq(), new Or(), new And())

  def toBool(v: Vertex): Option[Boolean] = {
    v.label match {
      case Label.V.vTrue => Some(true)
      case Label.V.vFalse => Some(false)
      case _ => None
    }
  }

  class Eq extends BinOpRewriter[Vertex] {
    override val label: Label = BinOp.EQUALS.toLabel

    override def calc(lhs: Vertex, rhs: Vertex): Option[Vertex] = {
      for {
        lhsV <- toBool(lhs)
        rhsV <- toBool(rhs)
      } yield Vertex.fromBoolean(lhsV == rhsV)
    }
  }

  class Neq extends BinOpRewriter[Vertex] {
    override val label: Label = BinOp.NOT_EQUALS.toLabel

    override def calc(lhs: Vertex, rhs: Vertex): Option[Vertex] = {
      for {
        lhsV <- toBool(lhs)
        rhsV <- toBool(rhs)
      } yield Vertex.fromBoolean(lhsV != rhsV)
    }
  }

  class Or extends BinOpRewriter[Vertex] {
    override val label: Label = BinOp.BOOL_OR.toLabel

    override def calc(lhs: Vertex, rhs: Vertex): Option[Vertex] = {
      for {
        lhsV <- toBool(lhs)
        rhsV <- toBool(rhs)
      } yield Vertex.fromBoolean(lhsV || rhsV)
    }
  }

  class And extends BinOpRewriter[Vertex] {
    override val label: Label = BinOp.BOOL_AND.toLabel

    override def calc(lhs: Vertex, rhs: Vertex): Option[Vertex] = {
      for {
        lhsV <- toBool(lhs)
        rhsV <- toBool(rhs)
      } yield Vertex.fromBoolean(lhsV && rhsV)
    }
  }

}


