package degrel.cluster

import akka.actor.ActorRef
import degrel.engine.Driver
import degrel.engine.namespace.Repository

class RemoteRepository(manager: ActorRef) extends Repository {
  val cache = new degrel.utils.collection.mutable.BiHashMap[List[Symbol], Driver]()
}

object RemoteRepository {
  def apply(manager: ActorRef): RemoteRepository = {
    new RemoteRepository(manager)
  }
}
