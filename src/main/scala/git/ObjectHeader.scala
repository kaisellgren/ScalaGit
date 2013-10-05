package git

import git.ObjectType.ObjectType

class ObjectHeader {
  var `type`: ObjectType = _
  var length = 0

  override def toString = s"ObjectHeader(length: $length, `type`: ${`type`})"
}

object ObjectHeader {
  def fromObjectFile(bytes: Array[Short]): ObjectHeader = {
    val o = new ObjectHeader

    // Figure out the object type. Read until we hit a space.
    val typeData = bytes.takeWhile(_ != 32)
    o.`type` = ObjectType.withName(new String(typeData))

    val lengthData = bytes.drop(typeData.length + 1).takeWhile(_ != 0)
    o.length = new String(lengthData).toInt

    o
  }
}