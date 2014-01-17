package degrel.front.dotlike

import scala.collection.mutable
import degrel.core.{Vertex, Edge}
import degrel.core
import degrel.front.LexicalContext

class DotlikeBuilder(ast: AstDigraph)(context: LexicalContext) {
  assert(!context.isPattern)

  val vertices = new mutable.HashMap[String, Vertex]
  val attributes = new mutable.HashMap[String, Map[String, String]]
  val edgeLabelMap = new mutable.HashMap[(String, String), String]
  val edgeDestinationMap = new mutable.HashMap[String, mutable.Set[String]] with mutable.MultiMap[String, String]
  val unprocessedAttributes = mutable.Buffer[AstDigraphAttributes]()

  ast.body.elements.foreach {
    case AstDigraphEmptyLine =>
    case edge: AstDigraphEdge => this.addEdge(edge)
    case attr: AstDigraphAttributes => {
      // Attribtesの追加は遅延させます
      unprocessedAttributes += attr
    }
  }

  unprocessedAttributes.foreach(this.addAttrs)

  private def addEdge(edge: AstDigraphEdge) = {
    val from = edge.fromLabel
    val to = edge.toLabel
    val label = edge.edgeLabel
    edgeLabelMap += (from -> to) -> label
    edgeDestinationMap.addBinding(from, to)
  }

  private def addAttrs(attrs: AstDigraphAttributes) = {
    val label = attrs.vertexLabel
    edgeDestinationMap.get(label) match {
      case None =>
      case Some(_) => throw new degrel.front.SyntaxError(s"Duplicated attribute definition for $label")
    }
  }

  private def vertexFor(label: String): Vertex = {
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
   * @param label 作成する頂点のラベル
   */
  private def createVertex(label: String): Unit = {
    val header = new core.VertexHeader(null)
    vertices += label -> header
    val edges = edgeDestinationMap.getOrElse(label, mutable.Seq[String]())
      .map(dstLabel => Edge(edgeLabelMap(label -> dstLabel),
                             this.vertexFor(dstLabel)))
    val body = new core.VertexBody(label, this.attributesFor(label), edges)
    header.write(body)
  }

  /**
   * 頂点labelに属するAttributesを返します
   */
  def attributesFor(label: String) = {
    attributes.getOrElse(label, Map())
  }


  def root: Vertex = {
    new core.VertexHeader({
      val label = ""
      val edges = edgeDestinationMap.getOrElse(label, mutable.Seq[String]())
        .map(dstLabel => Edge(edgeLabelMap(label -> dstLabel),
                               this.vertexFor(dstLabel)))
      new core.VertexBody(ast.label, this.attributesFor(label), edges)
    })
  }
}
