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

  // もし変数宣言の場合は変数に自分の名前を登録
  ast.name match {
    case AstName(Some(_), Some(cap)) => variables.bindSymbol(cap.expr, this)
  }

  // 各Edgeを作成．Edgeの先のVertexのBuilder[Vertex]をchildrenとする
  val (edges, children) = {
    val childBuilders = new mutable.MutableList[Primitive]
    (ast.edges.map(astEdge => {
      val vBuilder = factory.get[Vertex](this, astEdge.dst)
      childBuilders += vBuilder
      Edge(this.header, astEdge.label.expr, vBuilder.header)
    }), childBuilders.toSeq)
  }

  /**
   * @inheritdoc
   */
  override def variables: LexicalVariables = parent.variables

  /**
   * @inheritdoc
   */
  override val outerCell: CellBuilder = parent match {
    case cb: CellBuilder => cb
    case _ => parent.outerCell
  }

  /**
   * 参照頂点を作成し返します．参照先が見つからない場合は，{@code NameError}が送出されます
   * @param targetName
   * @return
   */
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

  /**
   * 通常の頂点を返します
   * @param labelExpr
   * @return
   */
  def plainVertex(labelExpr: String): VertexBody = {
    new VertexBody(Label(labelExpr), this.mkAttributesMap, this.edges, ID.NA)
  }

  /**
   * @inheritdoc
   */
  def concrete() = {
    val vb = ast.name match {
      case AstName(None, Some(cap)) => mkReferenceVertex(cap.expr)
      case AstName(Some(lbl), _) => plainVertex(lbl.expr) // A[foo](...) A(...)
      case _ => throw new CodeException("")
    }
    this.header.write(vb)
  }

  /**
   * この頂点におけるメタ属性の辞書を作成し返します
   * @return
   */
  def mkAttributesMap: Map[String, String] = {
    val srcattrs = ast.attributes match {
      case Some(attrs) => attrs.map(a => (a.key, a.value))
      case None => Map()
    }
    (srcattrs ++ this.mkSystemAttributes).toMap
  }

  /**
   * 構文解析器によって自動的に付与されるメタ属性を返します
   */
  def mkSystemAttributes: Iterable[(String, String)] = {
    ast.name match {
      case AstName(_, Some(AstVertexBinding(e))) => Seq("__captured_as__" -> e)
      case _ => Seq()
    }
  }
}
