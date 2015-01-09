package degrel.embed.builtins

import degrel.core.{Cell, Element}
import degrel.embed.CellFactory

import scala.collection.mutable

class StdoutFactory extends CellFactory {
  override def spawn(parent: Cell, args: mutable.MultiMap[String, Element]): Cell = ???
}
class Stdout {

}
