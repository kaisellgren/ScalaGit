package git

import git.ObjectType.ObjectType

case class ObjectHeader(
  `type`: ObjectType,
  length: Int = 0
) {
  override def toString = s"ObjectHeader(length: $length, `type`: ${`type`})"
}

object ObjectHeader {
  def fromObjectFile(bytes: Array[Short]): ObjectHeader = {
    // Figure out the object type. Read until we hit a space.
    val typeData = bytes.takeWhile(_ != 32)
    val t = ObjectType.withName(new String(typeData))

    val lengthData = bytes.drop(typeData.length + 1).takeWhile(_ != 0)
    val length = new String(lengthData).toInt

    ObjectHeader(`type` = t, length = length)
  }
}