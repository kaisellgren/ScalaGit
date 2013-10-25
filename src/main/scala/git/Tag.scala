package git

import java.util.Date
import git.TagType.TagType
import git.util.Parser._
import git.util.FileUtil._
import java.io.File
import git.util.DataReader

case class Tag(
  override val id: ObjectId,
  override val header: ObjectHeader,
  override val repository: Repository,
  taggerName: Option[String],
  taggerEmail: Option[String],
  tagDate: Option[Date],
  message: Option[String],
  tagName: String,
  tagType: TagType,
  targetIdentifier: ObjectId
) extends Object {
  def commit(): Commit = repository.database.findObjectById(targetIdentifier).get.asInstanceOf[Commit]
  def toObjectFile = Nil
}

object Tag {
  def fromObjectFile(bytes: List[Byte], repository: Repository, id: ObjectId, header: Option[ObjectHeader]): Tag = {
    val reader = new DataReader(bytes)

    if (reader.takeString(7) != "object ") throw new Exception("Corrupted Tag object.")

    // Followed by tag hash.
    val targetIdentifier = reader.takeObjectId()

    reader ++ 1 // LF.

    // Followed by the type.
    if (reader.takeString(5) != "type ") throw new Exception("Corrupted Tag object.")

    val tagType = TagType.withName(reader.takeStringWhile(_ != '\n'))

    reader ++ 1 // LF.

    // The tag type starts with "tag ", also skip.
    if (reader.takeString(4) != "tag ") throw new Exception("Corrupted Tag object.")

    val tagName = reader.takeStringWhile(_ != '\n')

    reader ++ 1 // LF.

    if (reader.takeString(7) != "tagger ") throw new Exception("Corrupted Tag object.")

    val tagger = parseUserFields(reader)

    // Finally the tag message, if it exists.
    val message = Option(reader.getRestAsString)

    Tag(
      id = id,
      repository = repository,
      header = header match {
        case Some(v) => v
        case None => ObjectHeader(ObjectType.Tag)
      },
      targetIdentifier = targetIdentifier,
      tagType = tagType,
      message = message,
      taggerName = Some(tagger.name),
      taggerEmail = Some(tagger.email),
      tagDate = Some(tagger.date),
      tagName = tagName
    )
  }

  def fromHashCode(hashCode: ObjectId, repository: Repository, name: String): Tag = {
    Tag(
      id = hashCode, // TODO: Wrong!
      repository = repository,
      header = ObjectHeader(ObjectType.Tag),
      tagType = TagType.Lightweight,
      message = None,
      taggerName = None,
      taggerEmail = None,
      tagDate = None,
      tagName = name,
      targetIdentifier = hashCode // TODO: Wrong!
    )
  }
}

object TagType extends Enumeration {
  type TagType = Value

  val Lightweight = Value("lightweight")
  val Annotated = Value("annotated")
  val Signed = Value("signed")
}
