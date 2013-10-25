package git

import scala.collection.mutable.ListBuffer

case class TreeEntry(mode: Int, name: String, id: ObjectId)

case class Tree(
  override val id: ObjectId,
  override val header: ObjectHeader,
  override val repository: Repository,
  entries: List[TreeEntry]
) extends Object {
  def toObjectFile = Nil
}

object Tree {
  def fromObjectFile(bytes: List[Byte], repository: Repository, id: ObjectId, header: Option[ObjectHeader]): Tree = {
    var data = bytes.drop(0)

    val entryBuilder = new ListBuffer[TreeEntry]

    def parseEntry() {
      val modeBytes = data.takeWhile(_ != 32)
      val mode = new String(modeBytes).toInt

      data = data.drop(modeBytes.length + 1)

      val nameBytes = data.takeWhile(_ != 0)
      val name = new String(nameBytes)

      data = data.drop(nameBytes.length + 1)

      val id = ObjectId.fromBytes(data.take(ObjectId.RawSize))

      data = data.drop(ObjectId.RawSize)

      entryBuilder += TreeEntry(mode = mode, name = name, id = id)
    }

    parseEntry()

    Tree(
      entries = entryBuilder.toList,
      header = header match {
        case Some(v) => v
        case None => ObjectHeader(ObjectType.Tree)
      },
      repository = repository,
      id = id
    )
  }
}
