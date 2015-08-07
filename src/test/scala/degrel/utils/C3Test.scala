package degrel.utils

import org.scalatest.FlatSpec

class C3Test extends FlatSpec {

  class Node(val parents: List[Node], val id: Int) {
    override def toString: String = s"node($id)"
  }

  class InjectableNode(var parents: List[InjectableNode], val id: Int)

  implicit object NodeHasParents extends HasParents[Node] {
    override def parents(node: Node): List[Node] = node.parents
  }

  implicit object InjectableNodeHasParents extends HasParents[InjectableNode] {
    override def parents(node: InjectableNode): List[InjectableNode] = node.parents
  }

  def node(i: Int, ps: Node*) = new Node(ps.toList, i)

  it should "Merge nodes" in {
    val root = node(0)
    val c1 = node(1, root)
    val c2 = node(2, root)
    val actual = C3.merge(List(List(c1, root), List(c2, root), List(c1, c2)))
    val expected = List(c1, c2, root)
    assert(actual === expected)
  }

  it should "Linearlize single node" in {
    val root = node(0)
    val actual = C3.linearize(root)
    val expected = List(root)
    assert(actual === expected)
  }

  it should "Linearize single parent node." in {
    val root = node(0, node(1))
    val actual = C3.linearize(root).map(_.id)
    val expectedIds = List(0, 1)
    assert(actual === expectedIds)
  }

  it should "Linearize line hierarchy" in {
    val root = node(0, node(1, node(2, node(3))))
    val actual = C3.linearize(root).map(_.id)
    val expectedIds = List(0, 1, 2, 3)
    assert(actual === expectedIds)
  }

  it should "Linearize diamond hierarchy" in {
    val root = node(0)
    val child = node(4, node(2, root), node(3, root))
    val actual = C3.linearize(child).map(_.id)
    val expectedIds = List(4, 2, 3, 0)
    assert(actual === expectedIds)
  }

  it should "Reject cycled graph" in {
    val n0 = new InjectableNode(List(), 0)
    val n1 = new InjectableNode(List(n0), 0)
    val n2 = new InjectableNode(List(n1), 0)
    n0.parents = List(n2)
    intercept[RuntimeException] {
      C3.linearize(n2)
    }
  }

}
