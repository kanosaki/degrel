package degrel.cluster

trait MemberRole {
  def name: String
}

object Roles {

  case object Controller extends MemberRole {
    override def name = "ctrl"
  }

  case object Worker extends MemberRole {
    override def name = "worker"
  }

  case object Lobby extends MemberRole {
    override def name = "lobby"
  }

}
