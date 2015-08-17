package degrel.engine

import degrel.core._
import degrel.engine.rewriting.{RewritingTarget, RewritingTarget$, ContinueRewriter, Rewriter}
import degrel.utils.collection.mutable.WeakMultiMap

/**
 * 書き換えを行う際パターンマッチの探索と実行を最適化するために
 * 書き換え対象の絞り込みを行うためのインターフェイス
 */
trait RewriteeSet extends Iterable[RewritingTarget] {
  def driver: Driver

  override def iterator: Iterator[RewritingTarget] = this.driver.header match {
    case c: Cell => CellTraverser(c, driver).iterator
    case _ => Seq(RewritingTarget.alone(driver.header, driver)).iterator
  }

  def targetsFor(rw: Rewriter): Iterable[RewritingTarget]

  def onWriteVertex(target: RewritingTarget, value: Vertex): Unit = {}

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
  override def targetsFor(rw: Rewriter): Iterable[RewritingTarget] = {
    (if (rw.isPartial)
      driver.rewriteTargets
    else
      driver.atomTargets) ++
      (if (rw.isMeta)
        Seq(RewritingTarget.alone(driver.header, driver))
      else
        Seq())
  }

  def name: String = "plain"
}

/**
 * 根のラベルを見て書き換え対象を絞り込む`RewriteeSet`
 */
class RootTableRewriteeSet(val driver: Driver) extends RewriteeSet {
  val labelMap = new WeakMultiMap[Label, RewritingTarget]()

  for (v <- driver.rewriteTargets) {
    labelMap.addBinding(v.target.label, v)
  }

  override def onWriteVertex(target: RewritingTarget, value: Vertex): Unit = {
    for (v <- CellTraverser(value, driver)) {
      labelMap.addBinding(v.target.label, v)
    }
  }

  override def targetsFor(rw: Rewriter): Iterable[RewritingTarget] = {
    val metaTargets = if (rw.isMeta) List(RewritingTarget.alone(driver.header, driver)) else List()
    if (rw.isPartial) {
      val tgts = labelMap.getOrElse(rw.pattern.label, List()).toList ++ metaTargets
      tgts
    } else {
      driver.atomTargets ++ metaTargets
    }
  }

  def name: String = "root_hash"
}
