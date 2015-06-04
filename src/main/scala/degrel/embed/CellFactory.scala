package degrel.embed

import degrel.core.{Element, Cell}

import scala.collection.mutable

trait CellFactory {
  def spawn(parent: Cell, args: mutable.MultiMap[String, Element]): Cell
}
