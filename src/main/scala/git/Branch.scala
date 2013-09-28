package git

class Branch {
  var repository: Repository = _
  var trackedBranch: Branch = _
  var isRemote: Boolean = _
  var tipId: ObjectId = _
  var name: String = _
  var canonicalName: String = _

  def tip: Commit = repository.database.findObjectById(tipId).asInstanceOf[Commit]
  def isTracking: Boolean = trackedBranch != null
  def commits: List[Commit] = Nil // TODO: Implement. Challenging.

  override def toString = s"Branch($canonicalName)"
}