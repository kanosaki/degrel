package degrel.front.dotlike

import degrel.core
import degrel.core.{Edge, ID, Vertex, VertexBody}
import degrel.front.LexicalContext
import degrel.utils.collection.mutable.BiHashMap

import scala.collection.mutable
import scalaz._

class DotlikeBuilder(ast: AstDigraph)(context: LexicalContext) {
  assert(!context.isPattern)

  val rootIdentifier = tagI("")
  val vertices = new mutable.HashMap[String @@ I, Vertex]
  val attributes = new mutable.HashMap[String @@ I, Map[String, String]]
  val edgeLabelMap = new mutable.HashMap[(String @@ I, String @@ I), String @@ L]
  val identifierLabelMap = new BiHashMap[String @@ I, String @@ L]()
  val edgeDestinationMap = new mutable.HashMap[String @@ I, mutable.Set[String @@ I]] with mutable.MultiMap[String @@ I, String @@ I]
  val unprocessedAttributes = mutable.Buffer[AstDigraphAttributes]()

  def addLazyInitVertex(label: String) = {

  }

  /**
   * 頂点labelに属するAttributesを返します
   */
  def attributesFor(label: String @@ I): Map[String, String] = {
    attributes.getOrElse(label, Map())
  }

  def root: Vertex = this.vertexFor(rootIdentifier)

  /**
   * DotLike構文木から，`AstDigraphEdge`を受け取って，一つの接続を
   * `DotLikeBuilder`に追加します．
   * @param edge 追加する
   */
  private def addEdge(edge: AstDigraphEdge): Unit = {
    val from = tagI(edge.fromLabel.toIdentifier)
    val to = tagI(edge.toLabel.toIdentifier)
    val label = tagL(edge.edgeLabel)
    edgeLabelMap += (from -> to) -> label
    edgeDestinationMap.addBinding(from, to)
    this.registerIdentifer(edge)
  }

  ast.body.elements.foreach(
  {
    case AstDigraphEmptyLine =>
    case edge: AstDigraphEdge => this.addEdge(edge)
    case attr: AstDigraphAttributes => {
      // Attribtesの追加は遅延させます
      unprocessedAttributes += attr
    }
  })

  // 根のラベルを登録
  identifierLabelMap += tagI("") -> tagL(ast.label)

  def registerIdentifer(edge: AstDigraphEdge): Unit = {
    identifierLabelMap += tagI(edge.fromLabel.toIdentifier) -> tagL(edge.fromLabel.label.getOrElse(""))
    identifierLabelMap += tagI(edge.toLabel.toIdentifier) -> tagL(edge.toLabel.label.getOrElse(""))
  }

  unprocessedAttributes.foreach(this.addAttrs)

  // Tag as L(Label)
  def tagL(expr: String): String @@ L = Tag[String, L](expr)

  private def addAttrs(attrs: AstDigraphAttributes) = {
    val label = tagI(attrs.vertexLabel)
    edgeDestinationMap.get(label) match {
      case None =>
      case Some(_) => throw new degrel.front.SyntaxError(s"Duplicated attribute definition for $label")
    }
  }

  // Tag as I(Identifier)
  def tagI(expr: String): String @@ I = Tag[String, I](expr)

  private def vertexFor(label: String @@ I): Vertex = {
    vertices.get(label) match {
      case Some(v) => v
      case None => {
        this.createVertex(label)
        vertices(label)
      }
    }
  }

  /**
   * ラベルlabelを持つ頂点を作成し，verticesにキャッシュします
   * ただし，VertexHeaderを先に作成し登録した後に，後からwriteを行います．
   * これは相互再帰的にvertexForを呼び出すため，再帰を止める働きを持ちます．
   * @param identifier 作成する頂点のラベル
   */
  private def createVertex(identifier: String @@ I): Unit = {
    val header = new core.VertexHeader(null)
    vertices += identifier -> header
    val edges = edgeDestinationMap.getOrElse(identifier, mutable.Seq[String @@ I]()).toSeq
      .map(dstLabel => Edge(header, Tag.unwrap(edgeLabelMap(identifier -> dstLabel)), {
      this.vertexFor(dstLabel)
    }))
    val body = VertexBody(Tag.unwrap(identifierLabelMap(identifier)), this.attributesFor(identifier), edges, ID.NA)
    header.write(body)
  }

  // Label Tag
  sealed trait L

  // Identifier Tag
  sealed trait I
}
