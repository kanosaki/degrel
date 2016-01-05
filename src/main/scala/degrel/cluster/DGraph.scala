package degrel.cluster

import degrel.core.{ID, Label}
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.JsonDSL._


case class DGraph(root: DID, vertices: Vector[DVertex], idMap: Vector[(DID, ID)]) {

  def pp: String = {
    val bodyMap = vertices.map(v => v.id -> v).toMap
    val headMap = idMap.toMap
    val sb = new StringBuilder()
    sb ++= s"Root: $root\n"
    vertices.foreach { v =>
      sb ++= f"${v.id} ${v.label} ${headMap.get(v.id).map { i => s"($i)" }.getOrElse("")}\n"
      v.edges.foreach { e =>
        val dstLabel = bodyMap.get(e.dst).map(_.label).getOrElse("--")
        sb ++= f"  | ${e.label} --> ${e.dst}($dstLabel)\n"
      }
    }
    sb.toString()
  }

  def toJson: JObject = {
    ("root" -> root) ~
      ("id_mapping" -> JObject(idMap.map { case (did, id) => (did.toString, JString(id.toString)) }.toList)) ~
      ("vertices" -> vertices.toList.map(v => (v.id.toString, v.toJson)))
  }
}

case class DEdge(label: String, dst: DID) {
  def toJson: JObject = {
    ("label" -> label) ~
      ("dst" -> dst)
  }
}

trait DVertex {
  def id: DID

  def label: String

  def attributes: Seq[(String, String)]

  def edges: Seq[DEdge]

  def toJson: JObject
}

case class DPlainVertex(id: DID,
                        label: String,
                        attributes: Seq[(String, String)],
                        edges: Seq[DEdge]) extends DVertex {
  override def toJson: JObject = {
    ("type" -> "plain") ~
      ("label" -> label) ~
      ("attributes" -> attributes) ~
      ("edges" -> edges.map(_.toJson))
  }
}

case class DRule(id: DID,
                 lhs: DID,
                 rhs: DID,
                 preds: Seq[DID],
                 pragmaEdges: Seq[DEdge],
                 attributes: Seq[(String, String)]) extends DVertex {

  override def label: String = Label.V.rule.expr

  override def edges: Seq[DEdge] = Seq(
    DEdge(Label.E.lhs.expr, this.lhs),
    DEdge(Label.E.rhs.expr, this.rhs)) ++ pragmaEdges

  override def toJson: JObject = {
    ("type" -> "rule") ~
      ("label" -> label) ~
      ("predicates" -> preds) ~
      ("pragmas" -> pragmaEdges.map(_.toJson)) ~
      ("attributes" -> attributes) ~
      ("edges" -> edges.map(_.toJson))
  }
}

case class DCell(id: DID,
                 attributes: Seq[(String, String)],
                 edges: Seq[DEdge],
                 binding: Seq[(ID, ID)]) extends DVertex {
  override def label: String = Label.V.cell.expr

  override def toJson: JObject = {
    ("type" -> "cell") ~
      ("label" -> label) ~
      ("binding" -> binding.map { case (k, v) => k.toString -> v.toString }) ~
      ("attributes" -> attributes) ~
      ("edges" -> edges.map(_.toJson))
  }
}


