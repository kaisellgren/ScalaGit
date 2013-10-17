package git

trait Object {
  def id: ObjectId
  def header: ObjectHeader
  def repository: Repository

  override def equals(o: Any) = o match {
    case that: Object => that.id.sha == id.sha
    case _ => false
  }

  override def hashCode = id.sha.hashCode
}