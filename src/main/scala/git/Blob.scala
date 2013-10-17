package git

case class Blob(
  override val id: ObjectId,
  override val header: ObjectHeader,
  override val repository: Repository,
  size: Int = 0,
  contents: List[Short] // TODO: We have to make sure we don't unnecessarily create blobs and consume memory...
) extends Object {
  def isBinary(): Boolean = {
    // TODO: Try to find at least one null byte.
    false
  }
}

object Blob {
  def fromObjectFile(bytes: Array[Short], id: ObjectId, repository: Repository, header: Option[ObjectHeader]): Blob = Blob(
    id = id,
    repository = repository,
    header = header match {
      case Some(v) => v
      case None => ObjectHeader(ObjectType.Blob)
    },
    size = bytes.length,
    contents = bytes.toList
  )
}