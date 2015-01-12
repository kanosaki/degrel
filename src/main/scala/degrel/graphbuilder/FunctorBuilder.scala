package degrel.graphbuilder

import degrel.core._
import degrel.front._

/**
 * AstFunctorからFunctorを構築するGraphBuilder
 * @param parent
 * @param ast
 */
class FunctorBuilder(val parent: Primitive, val ast: AstFunctor) extends Builder[Vertex] {
  self =>

  // Internal functor builder
  trait FBuilder {
    def header: Vertex

    def concrete(): Unit

    def edges: Seq[Edge] = {
      ast.edges.map(astEdge => {
        val builder = edgeChildMap(astEdge)
        Edge(this.header, astEdge.label.expr, builder.header)
      })
    }
  }

  private var builder: FBuilder = null

  def header: Vertex = builder.header

  /**
   * @inheritdoc
   */
  override val variables: LexicalVariables = parent.variables

  // もし変数宣言の場合は変数に自分の名前を登録
  ast.name match {
    case AstName(Some(_), Some(cap)) =>
      variables.bindSymbol(cap.expr, this)
    case _ =>
  }

  val edgeChildMap = ast.edges.
    map(astEdge => astEdge -> factory.get[Vertex](this, astEdge.dst)).
    toMap

  val children = edgeChildMap.values.toSeq

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
  def concreteReferenceVertex(targetName: String): Unit = {
    val vs = variables.resolveGrouped(targetName)
    vs match {
      case List(target) :: _ =>
        builder = new MirrorFunctor(target)
      case Nil :: xs => {
        xs.flatten.headOption match {
          case Some(target) =>
            builder = new ReferenceVertex(target)
          case None =>
            throw new CodeException(s"Undefined variable $targetName -- ${variables.toString}")
        }
      }
    }
  }

  /**
   * @inheritdoc
   */
  def doBuildPhase(phase: BuildPhase) = phase match {
    case MainPhase => {
      ast.name match {
        case AstName(None, Some(cap)) => {
          concreteReferenceVertex(cap.expr)
        }
        case AstName(Some(lbl), _) => {
          builder = new PlainVertex(lbl.expr)
        }
        case _ => throw new CodeException("")
      }
    }
    case FinalizePhase => {
      builder.concrete()
    }
    case _ =>
  }

  /**
   * この頂点におけるメタ属性の辞書を作成し返します
   * @return
   */
  def mkAttributesMap: Map[Label, String] = {
    val srcattrs = ast.attributes match {
      case Some(attrs) => attrs.map(a => (a.key, a.value))
      case None => Map()
    }
    Label.convertAttrMap(srcattrs ++ this.mkSystemAttributes)
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

  class PlainVertex(labelExpr: String) extends FBuilder {
    val header = new VertexHeader(null)

    override def concrete(): Unit = {
      this.header.write(
        new VertexBody(
          Label(labelExpr),
          self.mkAttributesMap,
          this.edges,
          ID.NA)
      )
    }
  }

  class ReferenceVertex(target: Primitive) extends FBuilder {
    val label = SpecialLabels.V_REFERENCE
    val header = new VertexHeader(null)

    def concrete() = {
      val refEdge = Edge(
        this.header,
        SpecialLabels.E_REFERENCE_TARGET,
        target.header
      )
      val vb = new ReferenceVertexBody(
        Label(label),
        self.mkAttributesMap,
        Stream(refEdge) ++ this.edges,
        ID.NA
      )
      this.header.write(vb)
    }
  }

  class MirrorFunctor(target: Primitive) extends FBuilder {
    override def header: Vertex = target.header

    override def concrete(): Unit = {}
  }

}
