package degrel.core.operators

import degrel.core._

import scala.collection.mutable
import scalaz._
import Scalaz._

// Ported from regrel/graph.py Graph.is_isomorphic
/**
 * 2つのグラフが同型かどうかを判別します．
 * その際，どの頂点が根かを考慮せずに全組み合わせを試行するので大きなグラフの比較は出来ないと思われます
 * @param self 対象のグラフ1つめ
 * @param that 対象のグラフ2つめ
 */
class GraphIsomorphismComparator(self: Graph, that: Graph) {

  // TODO: Refactoring
  def eval(): Boolean = {
    // 頂点や接続の数が異なれば同型ではない
    if (self.vertices.size != that.vertices.size) return false
    if (self.edges.size != that.edges.size) return false

    // 頂点と接続の数が0ならば同型
    if (self.vertices.size == 0 && self.edges.size == 0) return true

    // ラベルの列が等しくなければ同型では無い
    val self_vertices = self.vertices.map(_.label).sorted
    val that_vertices = that.vertices.map(_.label).sorted
    if (self_vertices != that_vertices) return false

    val self_attrs = self.edges.map(_.label).sorted
    val that_attrs = that.edges.map(_.label).sorted

    if (self_attrs != that_attrs) return false

    val edge_table = new mutable.HashMap[Vertex, mutable.HashMap[Label, mutable.Set[Vertex]]]

    for (e <- that.edges) {
      if (!edge_table.contains(e.src)) {
        edge_table += e.src -> new mutable.HashMap[Label, mutable.Set[Vertex]]()
      }
      if (!edge_table(e.src).contains(e.label)) {
        edge_table(e.src) += e.label -> new mutable.HashSet[Vertex]()
      }
      edge_table(e.src)(e.label).add(e.dst)
    }

    def isContain(src: Vertex, attr: Label, dst: Vertex): Boolean = {
      edge_table.get(src) >>= {_.get(attr)} >>= {_.apply(dst).some} match {
        case Some(tf) => tf
        case None => false
      }
    }

    // 元の実装がなんだか怪しいので解釈して書き換え
    val self_vs = vertexPermutations(self).head
    vertexPermutations(that).any(that_vertices => {
      val v_mapping: Map[Vertex, Vertex] = Map(self_vs.zip(that_vertices): _*)
      self.edges.forall(e => {
        val srcv = v_mapping(e.src)
        val dstv = v_mapping(e.dst)
        isContain(srcv, e.label, dstv)
      })
    })
  }

  def vertexPermutations(g: Graph): List[List[Vertex]] = {
    // 頂点vをソートする際に，3つ組(v.label, in_labels(v), out_labels(v))を用いてソートします
    // ただしin_labels(v)はvへの接続のラベルのリスト，out_labels(v)はvからの接続のラベルのリストです
    // そのときに用いるキーの型をVOrdKeyとして定義します
    type VOrdKey = (Label, Seq[Label], Seq[Label])
    implicit def VOrdKeyOrdering = new scala.math.Ordering[VOrdKey] {
      override def compare(x: VOrdKey, y: VOrdKey): Int = {
        implicit val seqordering = new degrel.utils.ordering.PythonSeqOrdering[Label]
        x._1.compareTo(y._1) match {
          case 0 => x._2.compareTo(y._2) match {
            case 0 => x._3.compareTo(y._3)
            case t => t
          }
          case s => s
        }
      }
    }

    // ソート用にグラフの全長点を走査してvertex_tableに追加してゆきます
    val vertex_table = new mutable.HashMap[VOrdKey, mutable.Set[Vertex]]() with mutable.MultiMap[VOrdKey, Vertex]
    for (v <- g.vertices) {
      val incomes: Seq[Label] = g.find_edges(dst = v).map(_.label).toSeq.sorted
      val outgoes: Seq[Label] = g.find_edges(src = v).map(_.label).toSeq.sorted
      vertex_table.addBinding((v.label, incomes, outgoes), v)
    }

    val sorted_vs: Seq[mutable.Set[Vertex]] = vertex_table
      .toSeq
      .sortBy(_._1.asInstanceOf[(Label, Seq[Label], Seq[Label])])
      .map(_._2)
    val perms: List[List[Seq[Vertex]]] = sorted_vs.map(_.toSeq.permutations.toList).toList
    perms.sequence.map(List.concat(_: _*))
  }
}
