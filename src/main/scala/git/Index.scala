package git

import java.util.Date

case class IndexHeader(version: Int, entryCount: Int)

case class IndexEntryStat(ctime: Date, mtime: Date, device: Int, inode: Int, mode: Int, uid: Int, gid: Int, size: Int)

case class IndexEntry(stat: IndexEntryStat, id: ObjectId, flags: List[Byte], name: String, padding: Int)

case class IndexExtension(name: List[Byte], size: Int, data: List[Byte])

case class Index(header: IndexHeader, checksum: String, contents: List[IndexEntry], extensions: List[IndexExtension])

object Index {
  def fromBytes(bytes: Array[Short]) = {

  }
}