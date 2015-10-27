package degrel.front.graphbuilder

import degrel.core._
import degrel.front.{AstBinExpr, AstCellPragma, BinOp}

import scala.collection.mutable

class RuleBuilder(val parent: Primitive, ast: AstBinExpr) extends Builder[Rule] {
  assert(ast.op == BinOp.RULE)

  override def typeLabel: Option[Label] = Some(Label.V.rule)

  /**
   * このグラフ要素における環境
   */
  override val variables: LexicalSymbolTable = parent.variables

  val lhsScope = new Scope(this)
  val rhsScope = new Scope(lhsScope)
  val lhsFactory = factory.get[Vertex](lhsScope, ast.left)
  val rhsFactory = factory.get[Vertex](rhsScope, ast.right)
  val pragmaChildren = mutable.ListBuffer[(String, Primitive)]()

  override def children = Seq(lhsFactory, rhsFactory) ++ pragmaChildren.map(_._2)

  /**
   * このグラフ要素への参照用のヘッダ
   */
  override val header: Rule = new RuleVertexHeader(null)

  /**
   * このグラフ要素を直接内包するCell
   */
  override def outerCell: CellBuilder = parent.outerCell

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   */
  override def doBuildPhase(phase: BuildPhase): Unit = phase match {
    case FinalizePhase => {
      val h = this.header.asInstanceOf[RuleVertexHeader]
      val pragmaEdges = pragmaChildren.map {
        case (lbl, dst) => Edge(this.header, Label(lbl), dst.header)
      }
      h.write(new RuleVertexBody(lhsFactory.header, rhsFactory.header, pragmaEdges))
    }
    case _ =>
  }

  override def addPragma(pragma: AstCellPragma): Unit = {
    pragma.edges.foreach { e =>
      pragmaChildren += e.actualLabel.expr -> factory.get[Vertex](this, e.dst)
    }
  }
}

