package git

import scala.collection.mutable.ListBuffer
import git.util.Conversion
import java.io.File

class PackIndex {
  var fanOutTable: List[Int] = _
  var objectIds: List[ObjectId] = _
  var offsets: List[Int] = _
  var packFile: File = _
  var length = 0
}

object PackIndex {
  def fromPackIndexFile(bytes: Array[Short]): PackIndex = {
    val o = new PackIndex

    var data = bytes

    // Confirm the header is correct.
    if (data.take(4).deep != Array(0xff, 0x74, 0x4f, 0x63).deep) throw new Exception("Index file header signature is corrupt.")

    data = data.drop(4)

    // Confirm the version.
    if (data.take(4).deep != Array(0, 0, 0, 2).deep) throw new Exception("Older Pack Index file format is not supported.")

    data = data.drop(4)

    // Create the fan-out table.
    val fanOutBuffer = new ListBuffer[Int]

    for (i <- 0 to 255) {
      fanOutBuffer += Conversion.bytesToValue(data.slice(i * 4, i * 4 + 4))
    }

    o.fanOutTable = fanOutBuffer.toList

    // Set the length (the last value of the fan-out table).
    o.length = o.fanOutTable.last

    data = data.drop(256 * 4)

    // Set the object id table.
    val objectIdBuffer = new ListBuffer[ObjectId]

    for (i <- 0 to o.length - 1) {
      objectIdBuffer += ObjectId.fromBytes(data.slice(i * 20, i * 20 + 20))
    }

    o.objectIds = objectIdBuffer.toList

    data = data.drop(o.length * 20)

    // Skip CRC32's for now.
    data = data.drop(o.length * 4)

    // Let's set the offsets.
    val offsetBuffer = new ListBuffer[Int]

    for (i <- 0 to o.length - 1) {
      offsetBuffer += Conversion.bytesToValue(data.slice(i * 4, i * 4 + 4)) // TODO: Implement support for very large offsets (>4 GB pack files).
    }

    o.offsets = offsetBuffer.toList

    o
  }
}