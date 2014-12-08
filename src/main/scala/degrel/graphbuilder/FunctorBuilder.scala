package degrel.graphbuilder

import degrel.core._
import degrel.front._

import scala.collection.mutable

/**
 * AstFunctorからFunctorを構築するGraphBuilder
 * @param parent
 * @param ast
 */
class FunctorBuilder(val parent: Primitive, val ast: AstFunctor) extends Builder[Vertex] {
  val header = new VertexHeader(null)

  ast.name match {
    case AstName(_, Some(cap)) => variables.bindSymbol(cap.expr, this)
  }

  val (edges, children) = {
    val childBuilders = new mutable.MutableList[Primitive]
    (ast.edges.map(astEdge => {
      val vBuilder = factory.get[Vertex](this, astEdge.dst)
      childBuilders += vBuilder
      Edge(this.header, astEdge.label.expr, vBuilder.header)
    }), childBuilders.toSeq)
  }

  override def variables: LexicalVariables = parent.variables

  override val outerCell: CellBuilder = parent match {
    case cb: CellBuilder => cb
    case _ => parent.outerCell
  }

  def mkReferenceVertex(targetName: String): VertexBody = {
    val label = SpecialLabel.Vertex.reference
    val targetBuilder = variables.resolveExact(targetName)
    val refEdge = Edge(this.header, SpecialLabels.E_REFERENCE_TARGET, targetBuilder.header)
    new VertexBody(
      Label(label),
      this.mkAttributesMap,
      Stream(refEdge) ++ this.edges,
      ID.NA)
  }

  def plainVertex(labelExpr: String): VertexBody = {
    new VertexBody(Label(labelExpr), this.mkAttributesMap, this.edges, ID.NA)
  }

  def concrete() = {
    val vb = ast.name match {
      case AstName(None, Some(cap)) => mkReferenceVertex(cap.expr)
      case AstName(Some(lbl), _) => plainVertex(lbl.expr) // A[foo](...) A(...)
      case _ => throw new CodeException("")
    }
    this.header.write(vb)
  }

  def mkAttributesMap: Map[String, String] = {
    val srcattrs = ast.attributes match {
      case Some(attrs) => attrs.map(a => (a.key, a.value))
      case None => Map()
    }
    (srcattrs ++ this.mkMetadataAttribtues).toMap
  }

  def mkMetadataAttribtues: Iterable[(String, String)] = {
    ast.name match {
      case AstName(_, Some(AstVertexBinding(e))) => Seq("__captured_as__" -> e)
      case _ => Seq()
    }
  }
}
