package git

import scala.collection.mutable.ListBuffer
import git.util.DataReader

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
    val reader = new DataReader(bytes)

    val entryBuilder = new ListBuffer[TreeEntry]

    def parseEntry() {
      val mode = reader.takeStringWhile(_ != ' ').toInt

      reader ++ 1 // Space.

      val name = reader.takeStringWhile(_ != 0)

      reader ++ 1 // Null.

      entryBuilder += TreeEntry(mode = mode, name = name, id = reader.takeObjectId())
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
