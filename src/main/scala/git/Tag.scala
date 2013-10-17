package git

import java.util.Date
import git.TagType.TagType
import git.util.Parser._
import git.util.FileUtil._
import java.io.File

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
  /*def findTagRef() = {
    targetIdentifier = ObjectId(new String(readContents(new File(repository.path + Reference.TagPrefix + tagName))))
  }*/

  def commit(): Commit = repository.database.findObjectById(targetIdentifier).get.asInstanceOf[Commit]
}

object Tag {
  def fromObjectFile(bytes: Array[Short], repository: Repository, id: ObjectId, header: Option[ObjectHeader]): Tag = {
    /*
      Example structure:
      "object" <SP> <HEX_OBJ_ID> <LF>
      "type" <SP> <OBJ_TYPE> <LF>
      "tag" <SP> <TAG_NAME> <LF>
      "tagger" <SP>
        <SAFE_NAME> <SP>
        <LT> <SAFE_EMAIL> <GT> <SP>
        <GIT_DATE> <LF>
      <LF>
      <DATA>
     */

    // The object file starts with "object ", let's skip that.
    var data = bytes.drop(7)

    // Followed by tag hash.
    val targetIdentifier = ObjectId.fromBytes(data.take(40))

    data = data.drop(40 + 1) // One LF.

    // The tag type starts with "type ", also skip.
    data = data.take(5)
    val tagType = TagType.withName(new String(data.takeWhile(_ != '\n')).trim)

    data = data.drop(40 + 1) // One LF.

    // The tag type starts with "tag ", also skip.
    data = data.take(4)
    val tagName = new String(data.takeWhile(_ != '\n')).trim

    data = data.drop(40 + 1) // One LF.

    data = data.drop(7) // Skip the "tagger " data.

    val taggerData = parseUserFields(data)
    val taggerName = Option(taggerData._1)
    val taggerEmail = Option(taggerData._2)
    val tagDate = Option(taggerData._3)
    data = taggerData._4

    // Finally the tag message, if it exists.
    val message = Option(new String(data).trim)

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
      taggerName = taggerName,
      taggerEmail = taggerEmail,
      tagDate = tagDate,
      tagName = tagName
    )
  }

  def fromHashCode(hashCode: ObjectId, repository: Repository): Tag = {
    Tag(
      id = hashCode,
      repository = repository,
      header = ObjectHeader(ObjectType.Tag),
      tagType = TagType.Lightweight,
      message = None,
      taggerName = None,
      taggerEmail = None,
      tagDate = None,
      tagName = "todo", // TODO
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
