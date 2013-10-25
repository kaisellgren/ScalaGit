package git

import java.util.{GregorianCalendar, Date}
import git.util.{Conversion, DataReader}
import scala.collection.mutable.ListBuffer

case class IndexHeader(version: Int, entryCount: Int)

case class IndexEntryStat(ctime: Date, mtime: Date, device: Int, inode: Int, mode: Int, uid: Int, gid: Int, size: Int)

case class IndexEntry(stat: IndexEntryStat, id: ObjectId, flags: List[Byte], name: String, padding: Int)

case class IndexExtension(name: List[Byte], size: Int, data: List[Byte])

case class Index(header: IndexHeader, checksum: ObjectId, entries: List[IndexEntry], extensions: List[IndexExtension])

object Index {
  def fromBytes(bytes: List[Byte]) = {
    val reader = new DataReader(bytes)

    if (reader.takeString(4) != "DIRC") throw new Exception("Corrupted Index file.")

    val header = IndexHeader(
      version = Conversion.bytesToValue(reader.take(4)),
      entryCount = Conversion.bytesToValue(reader.take(4))
    )

    if (header.version > 2) throw new Exception(s"Index file format version ${header.version} not supported!")

    /** Turns a two 4-byte lists into one `Date` object. */
    def createTime(seconds: List[Byte], nanoseconds: List[Byte]): Date = {
      val cal = new GregorianCalendar
      cal.setTimeInMillis(Conversion.bytesToValue(seconds) * 1000)
      cal.getTime
    }

    // Parse entries.
    val entryBuffer = new ListBuffer[IndexEntry]

    for (i <- 0 until header.entryCount) {
      // Construct the entry stat.
      val stat = IndexEntryStat(
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

      entryBuffer += IndexEntry(stat = stat, id = id, flags = flags, name = name, padding = padding)
    }

    // Extensions.
    val extensionBuffer = new ListBuffer[IndexExtension]

    while (reader.position < bytes.length - ObjectId.RawSize) {
      val signature = reader.take(4)
      val size = Conversion.bytesToValue(reader.take(4))
      val data = reader.take(size)
    }

    // Checksum.
    val checksum = reader.takeObjectId()

    Index(header = header, entries = entryBuffer.toList, checksum = checksum, extensions = extensionBuffer.toList)
  }
}