package degrel.engine

import degrel.core._
import degrel.engine.rewriting.{BasicRewriter, ContinueRewriter, Rewriter}
import degrel.utils.collection.mutable.WeakMultiMap

/**
 * 書き換えを行う際パターンマッチの探索と実行を最適化するために
 * 書き換え対象の絞り込みを行うためのインターフェイス
 */
trait RewriteeSet extends Iterable[Vertex] {
  def driver: Driver

  override def iterator: Iterator[Vertex] = this.driver.header match {
    case c: Cell => CellTraverser(c).iterator
    case _ => Seq(driver.header).iterator
  }

  def targetsFor(rw: Rewriter): Iterable[Vertex]

  def onWriteVertex(target: VertexHeader, value: Vertex): Unit = {}

  def onContinue(rw: ContinueRewriter): Unit = {}

  def name: String
}

object RewriteeSet {
  def create(name: String, driver: Driver): Option[RewriteeSet] = name match {
    case "plain" => Some(new PlainRewriteeSet(driver))
    case "root_table" => Some(new RootTableRewriteeSet(driver))
    case _ => None
  }
}


/**
 * 特に何もせず，元の書き換え対象をそのまま返す`RewriteeSet`
 */
class PlainRewriteeSet(val driver: Driver) extends RewriteeSet {
  override def targetsFor(rw: Rewriter): Iterable[Vertex] = {
    (if (rw.isPartial)
      driver.rewriteTargets
    else
      driver.itemRoots) ++
      (if (rw.isMeta)
        Seq(driver.header)
      else
        Seq())
  }

  def name: String = "plain"
}

/**
 * 根のラベルを見て書き換え対象を絞り込む`RewriteeSet`
 */
class RootTableRewriteeSet(val driver: Driver) extends RewriteeSet {
  val labelMap = new WeakMultiMap[Label, Vertex]()

  for (v <- driver.rewriteTargets) {
    labelMap.addBinding(v.label, v)
  }

  override def onWriteVertex(target: VertexHeader, value: Vertex): Unit = {
    for (v <- CellTraverser(value)) {
      labelMap.addBinding(v.label, v)
    }
  }

  override def targetsFor(rw: Rewriter): Iterable[Vertex] = {
    val metaTargets = if (rw.isMeta) List(driver.header) else List()
    if (rw.isPartial) {
      rw match {
        case brw: BasicRewriter => {
          val patternRootLabel = brw.rule.lhs.label
          labelMap(patternRootLabel).toList ++ metaTargets
        }
        case _ => {
          driver.rewriteTargets
        }
      }
    } else {
      driver.itemRoots
    }
  }

  def name: String = "root_hash"
}
