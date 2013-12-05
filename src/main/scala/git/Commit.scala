package git

import java.util.{Calendar, Date}
import git.util.Parser._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import git.util.DataReader

case class Commit(
  override val id: ObjectId,
  override val header: ObjectHeader,
  override val repository: Repository,
  authorName: String,
  authorEmail: String,
  authorDate: Date,
  committerName: String,
  committerEmail: String,
  commitDate: Date,
  message: String,
  treeId: ObjectId,
  parentIds: List[ObjectId]
) extends Object {
  def tree(): Tree = repository.database.findObjectById(treeId).get.asInstanceOf[Tree]

  def parents(): List[Commit] = Nil // TODO: Implement.
  def toObjectFile = Commit.toObjectFile(this)
}

object Commit {
  def toObjectFile(commit: Commit): List[Byte] = {
    val buffer = new ListBuffer[Byte]

    // TODO: Where do we create the ID?

    buffer.appendAll(s"tree ${commit.treeId.sha}\n".getBytes)

    commit.parentIds.foreach(id => {
      buffer.appendAll(s"parent ${id.sha}\n".getBytes)
    })

    buffer.appendAll(s"author ${commit.authorName} <${commit.authorEmail}> ${dateToGitFormat(commit.authorDate)}\n".getBytes)
    buffer.appendAll(s"committer ${commit.committerName} <${commit.committerEmail}> ${dateToGitFormat(commit.commitDate)}\n".getBytes)

    buffer.appendAll(s"\n${commit.message}".getBytes)

    // Insert the header in the beginning.
    buffer.insertAll(0, ObjectHeader(typ = ObjectType.Commit, length = buffer.length).toObjectFile())

    buffer.toList
  }

  def fromObjectFile(bytes: List[Byte], repository: Repository, id: ObjectId, header: Option[ObjectHeader]): Commit = {
    val reader = new DataReader(bytes)

    if (reader.takeString(5) != "tree ") throw new Exception("Corrupted Commit object file.")

    // Followed by tree hash.
    val treeId = reader.takeStringBasedObjectId()

    reader ++ 1 // LF.

    val parentIdsBuffer = new ListBuffer[ObjectId]

    // What follows is 0-n number of parent references.
    def parseParentIds() {
      // Stop if the data does not begin with "parent".
      if (reader.takeStringWhile(_ != ' ') == "parent") {
        reader ++ 1 // Space.

        parentIdsBuffer += reader.takeStringBasedObjectId()

        reader ++ 1 // LF.

        parseParentIds()
      }
    }

    parseParentIds()

    val parentIds = parentIdsBuffer.toList

    reader -- 6 // The parent ID parsing goes 6 bytes too far ("parent").

    if (reader.takeString(7) != "author ") throw new Exception("Corrupted Commit object file.")

    val author = parseUserFields(reader)

    if (reader.takeString(10) != "committer ") throw new Exception("Corrupted Commit object file.")

    val committer = parseUserFields(reader)

    // Finally the commit message.
    val message = new String(reader.getRest).trim

    Commit(
      id = id,
      header = header match {
        case Some(v) => v
        case None => ObjectHeader(ObjectType.Commit)
      },
      repository = repository,
      authorName = author.name,
      authorEmail = author.email,
      authorDate = author.date,
      committerName = committer.name,
      committerEmail = committer.email,
      commitDate = committer.date,
      message = message,
      treeId = treeId,
      parentIds = parentIds
    )
  }
}