package degrel.front.graphbuilder

import degrel.core._
import degrel.front._

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

  val (lhsFactory, predsFactory) = {
    def extractPreds(vAst: AstVertex): List[Primitive] = {
      vAst match {
        case AstBinExpr(astLhs, astOp, astRhs) if astOp.op == BinOp.PRED => {
          factory.get[Vertex](lhsScope, astLhs) :: extractPreds(astRhs)
        }
        case _ => List(factory.get[Vertex](lhsScope, vAst))
      }
    }
    ast.left match {
      case AstBinExpr(astLhs, astOp, astRhs) if astOp.op == BinOp.PRED => {
        (factory.get[Vertex](lhsScope, astLhs), extractPreds(astRhs).toSeq)
      }
      case _ => (factory.get[Vertex](lhsScope, ast.left), Seq())
    }
  }
  val rhsFactory = factory.get[Vertex](rhsScope, ast.right)
  val pragmaChildren = mutable.ListBuffer[(String, Primitive)]()

  override def children = Seq(lhsFactory, rhsFactory) ++ predsFactory ++ pragmaChildren.map(_._2)

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
      val preds = predsFactory.map(_.header)
      h.write(new RuleVertexBody(lhsFactory.header, rhsFactory.header, preds, pragmaEdges))
    }
    case _ =>
  }

  override def addPragma(pragma: AstCellPragma): Unit = {
    pragma.edges.foreach { e =>
      pragmaChildren += e.actualLabel.expr -> factory.get[Vertex](this, e.dst)
    }
  }
}

