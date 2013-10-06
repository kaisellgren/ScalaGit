package git

import java.io.{RandomAccessFile, File}
import git.util.Compressor

class PackFile {
  var file: File = _
  var index: PackIndex = _

  def loadObject(offset: Int, id: ObjectId): Object = {
    val raf = new RandomAccessFile(file, "r")
    raf.seek(offset)

    val bytes = new Array[Byte](1)
    raf.read(bytes)

    // The first byte includes type information, and also size info, which may continue in the next bytes.
    val firstByte = bytes(0) & 0xff

    // Figure out the type.
    val typeFlag = (firstByte >> 4) & 7

    // Figure out the length.
    def parseLength(index: Int, length: Int, shift: Int): Int = {
      val bytes = new Array[Byte](1)
      raf.read(bytes)

      val c = bytes(0) & 0xff
      val l = ((c & 0x7f) << shift) + length

      if ((c & 0x80) == 0) l //  We are not done until the most significant bit is 0.
      else parseLength(index = index + 1, length = l, shift = shift + 7)
    }

    val length = parseLength(shift = 4, length = firstByte & 0x0f, index = 1)

    // Retrieve the object data.
    val deflatedBytes = new Array[Byte](length)
    raf.read(deflatedBytes)
    val objectBytes = Compressor.decompressData(deflatedBytes.map(_.toShort))

    // Construct the header and the object.
    val header = new ObjectHeader

    val o = typeFlag match {
      case PackFile.CommitBitFlag => header.`type` = ObjectType.Commit; Commit.fromObjectFile(objectBytes)
      case PackFile.TreeBitFlag => header.`type` = ObjectType.Tree; Tree.fromObjectFile(objectBytes)
      case PackFile.BlobBitFlag => header.`type` = ObjectType.Blob; Blob.fromObjectFile(objectBytes)
      case PackFile.TagBitFlag => header.`type` = ObjectType.Tag; Tag.fromObjectFile(objectBytes)
      case _ => throw new Exception(s"Could not parse object type: $typeFlag") // TODO: Deltas
    }

    o.id = id
    o.header = new ObjectHeader

    o
  }
}

object PackFile {
  // Pre-defined bit flags.
  val ExtendedBitFlag = 0
  val CommitBitFlag = 1
  val TreeBitFlag = 2
  val BlobBitFlag = 3
  val TagBitFlag = 4
  val ReservedBitFlag = 5
  val OffsetDelta = 6
}