package git

import scala.collection.mutable.ListBuffer
import git.util.Conversion
import java.io.File

class PackIndex {
  var partialCounts: List[Int] = _
  var packFile: File = _
}

object PackIndex {
  def fromPackIndexFile(bytes: Array[Short]): PackIndex = {
    val o = new PackIndex

    var data = bytes

    // Create partial counts.
    val buffer = new ListBuffer[Int]

    // Confirm the header is correct.
    if (data.take(4).deep != Array(0xff, 0x74, 0x4f, 0x63).deep) throw new Exception("Index file header signature is corrupt.")

    data = data.drop(4)

    // Confirm the version.
    if (data.take(4).deep != Array(0, 0, 0, 2).deep) throw new Exception("Older Pack Index file format is not supported.")

    data = data.drop(4)

    for (i <- 0 to 255) {
      buffer += Conversion.bytesToValue(data.slice(i * 4, i * 4 + 4))
    }

    o.partialCounts = buffer.toList
    println(o.partialCounts)

    data = data.drop(256 * 4)

    o
  }
}