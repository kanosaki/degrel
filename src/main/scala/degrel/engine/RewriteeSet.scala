package degrel.engine

import java.io.PrintStream

import degrel.core._
import degrel.engine.rewriting.{Rewriter, RewritingTarget}
import degrel.utils.PrettyPrintOptions
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

  /**
   * `Rewriter`が書き換えられるかもしれない頂点を返します．
   * つまり，`rw: Rewriter`の`pattern`にマッチする可能性がある頂点を列挙します
   * 偽陽性は含まれる可能性がありますが，偽陰性はありません．
   * @param rw 対象となる`Rewriter`
   * @return 書き換えられる可能性のある`RewritingTarget`の集合
   */
  def targetsFor(rw: Rewriter): Iterable[RewritingTarget]

  /**
   * `Driver`において，書き込みが発生した際に呼ばれるコールバック
   */
  def onWriteVertex(target: RewritingTarget, value: Vertex): Unit = {}

  /**
   * `Driver`において，根の追加が発生したときに呼ばれるコールバック
   */
  def onAddRoot(targetCell: Cell, value: Vertex): Unit = {}

  /**
   * `Driver`において，根の削除が発生したときに呼ばれるコールバック
   */
  def onRemoveRoot(value: Vertex): Unit = {}

  /**
   * 名前．表示やインタプリタオプションの名前になります
   */
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
  val labelMap = new WeakMultiMap[Label, Vertex]()

  for (v <- driver.rewriteTargets) {
    labelMap.addBinding(v.target.label, v.root)
  }

  override def onRemoveRoot(value: Vertex): Unit = {
    for (v <- CellTraverser(value, driver)) {
      labelMap.removeBinding(v.target.label, value)
    }
  }

  override def onAddRoot(targetCell: Cell, value: Vertex): Unit = {
    for (v <- CellTraverser(value, driver)) {
      labelMap.addBinding(v.target.label, value)
    }
  }

  override def onWriteVertex(target: RewritingTarget, value: Vertex): Unit = {
    // 書き込まれる頂点のテーブルを削除します
    // TODO: root以外への書き込みの時は？
    if (target.target == target.root) {
      for (v <- CellTraverser(target.target, driver)) {
        labelMap.removeBinding(v.target.label, v.root)
      }
    }
    for (v <- CellTraverser(value, driver)) {
      labelMap.addBinding(v.target.label, target.root)
    }
  }

  def printTable(out: PrintStream = System.out): Unit = {
    implicit val ppo = PrettyPrintOptions(showAllId = true, multiLine = true)
    out.println(driver.header.pp)
    out.println(s"TableSize: ${labelMap.size}")
    labelMap.foreach {
      case (k, v) => {
        out.println(s"${k.expr} :: ${v.size}")
        out.println(s"  ${v.map(_.id)}")
      }
    }
  }

  override def targetsFor(rw: Rewriter): Iterable[RewritingTarget] = {
    val metaTargets = if (rw.isMeta) List(RewritingTarget.alone(driver.header, driver)) else List()
    if (rw.isPartial) {
      val candidates = labelMap.getOrElse(rw.pattern.label, List()).toSet
      candidates.flatMap { rt =>
        CellTraverser(rt, driver)
      } ++ metaTargets
    } else {
      driver.atomTargets ++ metaTargets
    }
  }

  def name: String = "root_hash"
}
