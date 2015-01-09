package degrel.core

package object operators {
  def duplicate(v: Vertex): Vertex = Duplicator(v)

  /**
   * REGRELにおけるデータの同値性とは？
   * グラフが同値 ::= グラフが同型(isomorphic)
   *
   * 1. グラフが等しい
   * グラフ = 頂点と接続の集合なので，すべての要素が対応付けできればよく，
   * すべての要素が複数の対応する要素を持っていい
   *
   * 2. 根が等しい
   * より厳密に表現すると「根とそこからのグラフが等しい」と表現できる
   * 「グラフが等しい」とほぼ同様にすべての要素が対応付けできればよいが，
   * 根のみが事前に対応する要素が指定されている．すなわちより厳しい条件となる
   *
   * グラフ比較では等しいが，根比較では等しくならない例:
   * (1) @a{-> b: l, b -> c: l, c -> :l}
   * (2) @b{-> c: l, c -> a: l, a -> :l}
   *
   * それぞれ，a -> b -> c -> (a) という相同なグラフを形成しますが
   * 根がa, bで異なるため，グラフとしては等しくなりますが，根付きグラフとしては異なるものになります
   */
  def areIsomorphic(one: Element, another: Element): Boolean = (one, another) match {
    case (v1: Vertex, v2: Vertex) => {
      new RootIsomorphismComparator(v1, v2).eval()
    }
    case (v1: RootedGraph, v2: RootedGraph) => {
      new RootIsomorphismComparator(v1.root, v2.root).eval()
    }
    case (v1: RootedGraph, v2: Vertex) => {
      new RootIsomorphismComparator(v1.root, v2).eval()
    }
    case (v1: Vertex, v2: RootedGraph) => {
      new RootIsomorphismComparator(v1, v2.root).eval()
    }
    case (g1: Graph, g2: Graph) => {
      new GraphIsomorphismComparator(g1, g2).eval()
    }
    case _ => throw new Exception("")
  }


  /**
   * グラフのインタプリタ上での表現を正規化します．
   * 例えば，Vertexのみで構成されているグラフから，Cellを表現している部分は
   * Cellクラスで，Ruleを表現している部分はRuleクラスで表し正規化します．
   */
  def normalize(v: Vertex): Vertex = {
    ???
  }
}
