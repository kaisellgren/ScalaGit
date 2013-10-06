package git

// TODO: Consider renaming... git.GitObject?
class Object {
  var id: ObjectId = _
  var header: ObjectHeader = _
  var repository: Repository = _

  override def equals(o: Any) = o match {
    case that: Object => that.id.sha == id.sha
    case _ => false
  }

  override def hashCode = id.sha.hashCode
}