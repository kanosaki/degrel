package degrel.core

import degrel.DegrelException
import degrel.utils.PrettyPrintOptions

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import degrel.core.utils.PrettyPrinter
import degrel.engine.rewriting.matching.{Matcher, MatchingContext, VertexMatching}

trait Vertex extends Element with Comparable[Vertex] {
  def edges: Iterable[Edge]

  def attributes: Map[Label, String]

  def attr(key: Label): Option[String]

  def id: ID

  def label: Label

  def shallowCopy(): Vertex

  override def pp(implicit opt: PrettyPrintOptions): String = new PrettyPrinter(this).apply()

  def edgesWith(label: Label = Label.V.wildcard): Iterable[Edge] = {
    label match {
      case Label.V.wildcard => this.edges
      case _ => this.edges.filter(_.label == label)
    }
  }

  def hasAttr(key: Label, value: String = null) = {
    value match {
      case null => this.attr(key).isDefined
      case v => this.attr(key) match {
        case None => false
        case Some(actualValue) => actualValue == v
      }
    }
  }

  def groupedEdges: Iterable[Iterable[Edge]] = {
    this.edges.groupBy(_.label).values
  }

  def hasEdge(label: Label = Label.V.wildcard): Boolean = {
    this.edgesWith(label).nonEmpty
  }

  def matches(pattern: Vertex, context: MatchingContext = MatchingContext.empty): VertexMatching = {
    Matcher(this).matches(pattern, context)
  }

  def isReference: Boolean = this.label == Label.V.reference && this.hasEdge(Label.E.ref)

  def isRule: Boolean =
    this.label == Label.V.rule &&
      this.hasEdge(Label.E.rhs) &&
      this.hasEdge(Label.E.lhs)

  def isCell: Boolean = this.label == Label.V.cell

  def isValue: Boolean = false

  def getValue[T: TypeTag]: Option[T] = None

  def thruSingle(label: Label): Vertex = {
    val candidates = this.edgesWith(label)
    candidates.size match {
      case 1 => candidates.head.dst
      case 0 => throw new Exception(s"No edge '${label.expr}' found.")
      case _ => throw new Exception(s"Too many edge '${label.expr}' found.")
    }
  }

  def thru(label: Label): Iterable[Vertex] = {
    this.edgesWith(label).map(_.dst)
  }

  def thru(index: Int): Iterable[Vertex] = {
    this.thru(Label(index.toString))
  }

  def thru(pred: Edge => Boolean): Iterable[Vertex] = {
    this.edges.filter(pred).map(_.dst)
  }

  def asRule: Rule = {
    val rhs = this.thruSingle(SpecialLabels.E_RHS)
    val lhs = this.thruSingle(SpecialLabels.E_LHS)
    Rule(lhs, rhs)
  }

  def asCell: Cell = {
    require(this.label == Label.V.cell, s"${this.label.expr} is not cell")
    Cell(this.edges)
  }

  def asHeader: VertexHeader = {
    this.asInstanceOf[VertexHeader]
  }

  def unhead[B <: VertexBody : ClassTag]: B = {
    this match {
      case vh: VertexHeader if implicitly[ClassTag[B]].runtimeClass.isInstance(vh.body) => {
        vh.body.asInstanceOf[B]
      }
      case vb: B => {
        vb
      }
      case _ => {
        throw new RuntimeException(s"Cannot unreference $this(${this.getClass}}) as ${implicitly[ClassTag[B]].runtimeClass}")
      }
    }
  }

  /**
   * 自分が`ReferenceVertex`の時は，参照を辿って`B`のインスタンスを返します
   *
   * @tparam B
   * @return
   */
  def unref[B <: Vertex : ClassTag]: B = {
    if (!this.isReference) {
      this.asInstanceOf[B]
    } else {
      val neighbors = this.thru(Label.E.ref)
        .filter(implicitly[ClassTag[B]].runtimeClass.isInstance).toList
      if (neighbors.length == 1) {
        neighbors.head.asInstanceOf[B]
      } else {
        throw DegrelException(s"Malformed Reference Vertex! $this")
      }
    }
  }

  def toGraph: Graph = {
    this match {
      case g: Graph => g
      case _ => new RawRootedGraph(this)
    }
  }

  def compareTo(o: Vertex): Int = {
    this.id.compareTo(o.id)
  }

  def version: VertexVersion = {
    this match {
      case vh: VertexHeader => VertexVersion(vh, vh.body)
      case _ => throw new Exception("VertexVersion is only available for VertexHeader.")
    }
  }

  override def toString: String = utils.pp(this)

  def neighbors: Iterable[Vertex] = {
    this.edges.map(_.dst)
  }

  def next(label: Label): Iterable[Vertex] = {
    this.neighbors.filter(_.label == label)
  }

  def next(pred: Vertex => Boolean): Iterable[Vertex] = {
    this.neighbors.filter(pred)
  }

  def hash: VertexHash = {
    VertexHash(this)
  }
}

object Vertex {
  def apply(label: String,
            edges: Iterable[Edge],
            attributes: Map[Label, String] = Map(),
            id: ID = ID.NA): Vertex = {
    val body = VertexBody(Label(label), attributes, edges.toSeq, id)
    new LocalVertexHeader(body)
  }

  /**
   * Note: edgeInitに渡されるVertexはVertexHeaderのみなので注意
   * @param label
   * @param attributes
   * @param id
   * @param edgeInit
   * @return
   */
  def create(label: Label,
             attributes: Map[Label, String] = Map(),
             id: ID = ID.NA)
            (edgeInit: Vertex => Iterable[Edge]): Vertex = {
    val vh = new LocalVertexHeader(null)
    val edges = edgeInit(vh)
    val body = VertexBody(label, attributes, edges.toSeq, id)
    vh.write(body)
    vh
  }

  def fromBoolean(b: Boolean): Vertex = b match {
    case true => vTrue
    case false => vFalse
  }

  // Special vertices
  val vTrue = Vertex("true", Seq())
  val vFalse = Vertex("false", Seq())
  val vNil = Cell()
}
