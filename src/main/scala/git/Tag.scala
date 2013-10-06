package git

import java.util.Date
import git.TagType.TagType
import git.util.Parser._
import git.util.FileUtil._
import java.io.File

class Tag extends Object {
  var taggerName: String = _
  var taggerEmail: String = _
  var tagDate: Date = _
  var message: Option[String] = _
  var tagName: String = _
  var tagType: TagType = _

  var targetIdentifier: ObjectId = _

  def findTagRef() = {
    val tagFile = new File(repository.path + Reference.TagPrefix + tagName)
    targetIdentifier = ObjectId(new String(readContents(tagFile)))
  }

  def commit: Commit = repository.database.findObjectById(targetIdentifier).asInstanceOf[Commit]
}

object Tag {
  def fromObjectFile(bytes: Array[Short]): Tag = {
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

    val o = new Tag

    // The object file starts with "object ", let's skip that.
    var data = bytes.drop(7)

    // Followed by tag hash.
    o.targetIdentifier = ObjectId.fromBytes(data.take(40))

    data = data.drop(40 + 1) // One LF.

    // The tag type starts with "type ", also skip.
    data = data.take(5)
    o.tagType = TagType.withName(new String(data.takeWhile(_ != '\n')).trim)

    data = data.drop(40 + 1) // One LF.

    // The tag type starts with "tag ", also skip.
    data = data.take(4)
    o.tagName = new String(data.takeWhile(_ != '\n')).trim

    data = data.drop(40 + 1) // One LF.

    data = data.drop(7) // Skip the "tagger " data.

    val taggerData = parseUserFields(data)
    o.taggerName = taggerData._1
    o.taggerEmail = taggerData._2
    o.tagDate = taggerData._3
    data = taggerData._4

    // Finally the tag message, if it exists.
    o.message = Option(new String(data).trim)

    o
  }

  def fromHashCode(hashCode: ObjectId): Tag = {
    val tag = new Tag
    tag.targetIdentifier = hashCode
    tag
  }
}

object TagType extends Enumeration {
  type TagType = Value

  val Lightweight = Value("lightweight")
  val Annotated = Value("annotated")
  val Signed = Value("signed")
}
