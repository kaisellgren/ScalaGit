package git

sealed trait BaseBranch {
  def repository: Repository
  def trackedBranch: Option[BaseBranch]
  def tipId: ObjectId
  def name: String
  def canonicalName: String

  def tip(): Commit = repository.database.findObjectById(tipId).get.asInstanceOf[Commit]
  def isTracking: Boolean = !trackedBranch.isEmpty
  def commits(): List[Commit] = repository.commits.find(CommitFilter(since = Some(List(this))))
}

case class Branch(
  repository: Repository,
  trackedBranch: Option[BaseBranch],
  tipId: ObjectId,
  name: String,
  canonicalName: String
) extends BaseBranch

case class RemoteBranch(
 repository: Repository,
 trackedBranch: Option[BaseBranch],
 tipId: ObjectId,
 name: String,
 canonicalName: String
) extends BaseBranch

case class DetachedHead(repository: Repository, tipId: ObjectId) extends BaseBranch {
  def name = "(no branch)"
  def canonicalName = "(no branch)"
  def trackedBranch = None
}