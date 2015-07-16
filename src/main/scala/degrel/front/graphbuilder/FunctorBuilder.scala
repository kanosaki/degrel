package degrel.front.graphbuilder

import degrel.core._
import degrel.front._

/**
 * AstFunctorからFunctorを構築するGraphBuilder
 */
class FunctorBuilder(val parent: Primitive, val ast: AstFunctor) extends Builder[Vertex] {
  self =>

  // Internal functor builder
  trait FBuilder {
    def header: Vertex

    def concrete(): Unit

    def edges: Seq[Edge] = {
      val othersEdges: Seq[Edge] = ast.edges.others.collect {
        case oe if ! oe.isDeclare => {
          variables.lookupTyped(oe.binding.expr, LexicalType.OthersVertex) match {
            case foundOVertex: LookupFound[Builder[Vertex]] => {
              Edge(this.header, Label.E.others, foundOVertex.primary.header)
            }
            case _ => variables.lookupTyped(oe.binding.expr, LexicalType.Vertex) match {
              case foundVertex: LookupFound[Builder[Vertex]] => {
                Edge(this.header, Label.E.include, foundVertex.primary.header)
              }
              case _ => throw new Exception(s"Undefined variable: ${oe.binding}(others edges)")
            }
          }
        }
      }
      val plainEdges = ast.edges.plains.map(astEdge => {
        val builder = edgeChildMap(astEdge)
        Edge(this.header, astEdge.label.expr, builder.header)
      })
      plainEdges ++ othersEdges
    }
  }

  private var builder: FBuilder = null

  def header: Vertex = builder.header

  override def typeLabel: Option[Label] = ast.name.labelExpr.flatMap(l => Some(Label(l)))

  /**
   * @inheritdoc
   */
  override val variables: LexicalSymbolTable = parent.variables

  // もし変数宣言の場合は変数に自分の名前を登録
  ast.name match {
    case AstName(Some(_), Some(cap)) =>
      variables.bind(cap.expr, this, LexicalType.Vertex)
    case _ =>
  }

  ast.edges.others.foreach {
    case oe if oe.isDeclare =>
      variables.bind(oe.binding.expr, this, LexicalType.OthersVertex)
    case _ =>
  }

  val edgeChildMap = ast.edges.plains.
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
    val vs = variables.resolveTyped(targetName, LexicalType.Vertex)
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
      case Some(attrs) => attrs.map(a => (Label(a.key), a.value))
      case None => Map()
    }
    Label.convertAttrMap(srcattrs ++ this.mkSystemAttributes)
  }

  /**
   * 構文解析器によって自動的に付与されるメタ属性を返します
   */
  def mkSystemAttributes: Iterable[(Label, String)] = {
    ast.name match {
      case AstName(_, Some(AstBinding(e))) => Seq(Label.A.capturedAs -> e)
      case _ => Seq()
    }
  }

  /**
   * 通常の頂点を作成します
   * @param labelExpr 頂点のラベル
   */
  class PlainVertex(labelExpr: String) extends FBuilder {
    val header = VertexHeader(null)

    override def concrete(): Unit = {
      this.header.write(
        VertexBody(
          Label(labelExpr),
          self.mkAttributesMap,
          this.edges,
          ID.NA)
      )
    }
  }

  /**
   * DEGRELにおける参照頂点を作成します
   * @param target 参照先の`Builder`
   */
  class ReferenceVertex(target: Primitive) extends FBuilder {
    val label = SpecialLabels.V_REFERENCE
    val header = VertexHeader(null)

    def concrete() = {
      val refEdge = Edge(
        this.header,
        SpecialLabels.E_REFERENCE_TARGET,
        target.header
      )
      val vb = new ReferenceVertexBody(
        Label(label),
        self.mkAttributesMap,
        Stream(refEdge) ++ this.edges
      )
      this.header.write(vb)
    }
  }

  /**
   * ミラー頂点を構成します
   *
   * ミラー頂点は，同一スコープ内の変数参照で
   * pat@X -> foo(bar: @Y, baz: Y, hoge: X)
   * というような規則があった場合，`X`は別のレベル(書き換え規則の右辺左辺)であるので
   * 通常の参照頂点となりますが，`Y`は同一レベルであるのでミラー頂点です．
   * 参照頂点は"参照頂点"という特別な頂点をグラフ上に表現しますが，ミラー頂点は
   * 変数宣言で生成された頂点と同じ参照を返します．
   */
  class MirrorFunctor(target: Primitive) extends FBuilder {
    override def header: Vertex = target.header

    override def concrete(): Unit = {}
  }

}
