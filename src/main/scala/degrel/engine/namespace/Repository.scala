package degrel.engine.namespace

import scala.collection.mutable

class Repository {
  val map = mutable.HashMap[Name, Content]()

  def register(name: Name, value: Content): Unit = {
    map += name -> value
  }

  def get(name: Name): Option[Content] = {
    map.get(name)
  }
}
