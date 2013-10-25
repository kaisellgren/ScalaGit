package git

import git.ObjectType.ObjectType
import scala.collection.mutable.ListBuffer

case class ObjectHeader(`type`: ObjectType, length: Int = 0) {
  def toObjectFile(): List[Byte] = ObjectHeader.toObjectFile(this)
}

object ObjectHeader {
  def toObjectFile(header: ObjectHeader): List[Byte] = {
    val buffer = new ListBuffer[Byte]

    buffer.appendAll(s"${header.`type`} ".getBytes)
    buffer.appendAll(s"${header.length}".getBytes)
    buffer.append(0)

    buffer.toList
  }

  def fromObjectFile(bytes: List[Byte]): ObjectHeader = {
    // Figure out the object type. Read until we hit a space.
    val typeData = bytes.takeWhile(_ != 32)
    val t = ObjectType.withName(new String(typeData))

    val lengthData = bytes.drop(typeData.length + 1).takeWhile(_ != 0)
    val length = new String(lengthData).toInt

    ObjectHeader(`type` = t, length = length)
  }
}