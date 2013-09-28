package git

case class Reference(canonicalName: String, targetIdentifier: ObjectId) {
  //var remote: Remote = _ // TODO: Implement remotes
  var remote: String = _

  override def toString = s"Reference(canonicalName: $canonicalName, targetIdentifier: $targetIdentifier)"
}

object Reference {
  val LocalBranchPrefix = "/refs/heads/"
  val RemoteTrackingBranchPrefix = "/refs/remotes/"
  val TagPrefix = "/refs/tags/"
  val NotePrefix = "/refs/notes/"
}