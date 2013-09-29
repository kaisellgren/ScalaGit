package git

import java.util.Date
import git.TagType.TagType
import git.util.Parser._

class Tag extends Object {
  var taggerName: String = _
  var taggerEmail: String = _
  var tagDate: Date = _
  var message: String = _
  var tagId: ObjectId = _
  var tagName: String = _
  var tagType: TagType = _
}

object Tag {
  def fromObjectFile(bytes: Array[Byte]): Tag = {
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
    o.tagId = ObjectId.fromHash(new String(data.take(40)))

    data = data.drop(40 + 1) // One LF.

    // The tag type starts with "type ", also skip
    data = data.take(5)
    //o.tagType = TagType(new String(data.takeWhile(_ != '>')).trim)

    data = data.drop(40 + 1) // One LF.

    // The tag type starts with "tag ", also skip
    data = data.take(4)
    o.tagName = new String(data.takeWhile(_ != '>')).trim

    data = data.drop(40 + 1) // One LF.

    data = data.drop(7) // Skip the "tagger " data.

    val taggerData = parseUserFields(data)
    o.taggerName = taggerData._1
    o.taggerEmail = taggerData._2
    o.tagDate = taggerData._3
    data = taggerData._4

    // Finally the tag message, if it exists.
    o.message = new String(data).trim
    o
  }
}

object TagType extends Enumeration {
  type TagType = Value

  val Lightweight = Value("lightweight")
  val Annotated = Value("annotated")
  val Signed = Value("signed")
}
