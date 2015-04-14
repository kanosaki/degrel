package degrel.engine.namespace

class Repository {
  val map = new degrel.utils.collection.mutable.BiHashMap[Name, Content]()

  def getName(value: Content): Name = {
    map.fromValue(value).get
  }

  def register(name: Name, value: Content): Unit = {
    map += name -> value
  }

  def register(name: String, value: Content): Unit = {
    map += List(Symbol(name)) -> value
  }

  def get(name: Name): Option[Content] = {
    map.get(name)
  }
}

object Repository {
  def apply(): Repository = {
    new Repository()
  }
}
