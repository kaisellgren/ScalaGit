package git

import scala.collection.mutable.ListBuffer
import git.util.{DataReader, Conversion}
import java.io.File

class PackIndex {
  var fanOutTable: List[Int] = _
  var objectIds: List[ObjectId] = _
  var offsets: List[Int] = _
  var packFile: PackFile = _
  var length = 0

  def has(id: ObjectId) = objectIds.contains(id)

  def getOffset(id: ObjectId): Option[Int] = {
    if (has(id)) Some(offsets(objectIds.indexOf(id)))
    else None
  }
}

object PackIndex {
  def fromPackIndexFile(bytes: List[Byte]): PackIndex = {
    val o = new PackIndex

    val reader = new DataReader(bytes)

    // Confirm the header is correct.
    if (reader.take(4).toArray.deep != Array(0xff, 0x74, 0x4f, 0x63).map(_.toByte).deep) throw new Exception("Index file header signature is corrupt.")

    // Confirm the version.
    if (reader.take(4).toArray.deep != Array(0, 0, 0, 2).map(_.toByte).deep) throw new Exception("Older Pack Index file format is not supported.")

    // Create the fan-out table.
    val fanOutBuffer = new ListBuffer[Int]

    for (i <- 0 to 255) fanOutBuffer += Conversion.bytesToValue(reader.take(4))

    o.fanOutTable = fanOutBuffer.toList

    // Set the length (the last value of the fan-out table).
    o.length = o.fanOutTable.last

    // Set the object id table.
    val objectIdBuffer = new ListBuffer[ObjectId]

    for (i <- 0 to o.length - 1) objectIdBuffer += reader.takeObjectId()

    o.objectIds = objectIdBuffer.toList

    // Skip CRC32's for now.
    reader ++ o.length * 4

    // Let's set the offsets.
    val offsetBuffer = new ListBuffer[Int]

    // TODO: Implement support for very large offsets (>4 GB pack files).
    for (i <- 0 to o.length - 1) offsetBuffer += Conversion.bytesToValue(reader.take(4))

    o.offsets = offsetBuffer.toList

    o
  }
}