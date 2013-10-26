package git

import java.util.{GregorianCalendar, Date}
import git.util.{Conversion, DataReader}
import scala.collection.mutable.ListBuffer

sealed case class IndexFileHeader(version: Int, entryCount: Int)

sealed case class IndexFileEntryStat(ctime: Date, mtime: Date, device: Int, inode: Int, mode: Int, uid: Int, gid: Int, size: Int)

sealed case class IndexFileEntry(stat: IndexFileEntryStat, id: ObjectId, flags: List[Byte], name: String, padding: Int)

sealed case class IndexFileExtension(name: List[Byte], size: Int, data: List[Byte])

case class IndexFile(header: IndexFileHeader, checksum: ObjectId, entries: List[IndexFileEntry], extensions: List[IndexFileExtension])

object IndexFile {
  def fromBytes(bytes: List[Byte]) = {
    val reader = new DataReader(bytes)

    if (reader.takeString(4) != "DIRC") throw new Exception("Corrupted Index file.")

    val header = IndexFileHeader(
      version = Conversion.bytesToValue(reader.take(4)),
      entryCount = Conversion.bytesToValue(reader.take(4))
    )

    if (header.version > 2) throw new Exception(s"Index file format version ${header.version} not supported.")

    /** Turns a two 4-byte lists into one `Date` object. */
    def createTime(seconds: List[Byte], nanoseconds: List[Byte]): Date = {
      val cal = new GregorianCalendar
      cal.setTimeInMillis(Conversion.bytesToValue(seconds) * 1000)
      cal.getTime
    }

    // Parse entries.
    val entryBuffer = new ListBuffer[IndexFileEntry]

    for (i <- 0 until header.entryCount) {
      // Construct the entry stat.
      val stat = IndexFileEntryStat(
        ctime = createTime(reader.take(4), reader.take(4)),
        mtime = createTime(reader.take(4), reader.take(4)),
        device = Conversion.bytesToValue(reader.take(4)),
        inode = Conversion.bytesToValue(reader.take(4)),
        mode = Conversion.bytesToValue(reader.take(4)),
        uid = Conversion.bytesToValue(reader.take(4)),
        gid = Conversion.bytesToValue(reader.take(4)),
        size = Conversion.bytesToValue(reader.take(4))
      )

      val id = reader.takeObjectId()

      val flags = reader.take(2)

      if (header.version >= 3) {
        // TODO:
        val moreFlags = reader.take(2)
      }

      // Path name.
      val name = if (header.version < 4) {
        reader.takeStringWhile(_ != 0)
      } else {
        "" // TODO:
      }

      // Null padding 1-8 bytes, retaining the total size in multiple of eight.
      val total = if (62 + name.length % 8 == 0) 62 + name.length + 8 else 62 + name.length
      val padding = 8 - (total % 8)
      reader ++ padding // TODO: Is this needed somewhere (other than for a marker purposes)?

      entryBuffer += IndexFileEntry(stat = stat, id = id, flags = flags, name = name, padding = padding)
    }

    // Extensions.
    val extensionBuffer = new ListBuffer[IndexFileExtension]

    while (reader.position < bytes.length - ObjectId.RawSize) {
      val signature = reader.take(4)
      val size = Conversion.bytesToValue(reader.take(4))
      val data = reader.take(size)
      println(signature)
      println(size)
      println(new String(data.toArray))
    }

    // Checksum.
    val checksum = reader.takeObjectId()

    IndexFile(header = header, entries = entryBuffer.toList, checksum = checksum, extensions = extensionBuffer.toList)
  }
}