package git

import scala.collection.mutable.ListBuffer
import java.util.Date
import git.TagType.TagType

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
    o
  }
}

object TagType extends Enumeration {
  type TagType = Value

  val Lightweight = Value("lightweight")
  val Annotated = Value("annotated")
  val Signed = Value("signed")
}
