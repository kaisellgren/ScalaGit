/*
 * Copyright (c) 2014 the original author or authors.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package git

import git.util.{Conversion, DataReader, PathUtil, FileUtil}
import java.io.File
import java.util.{GregorianCalendar, Date}
import java.security.MessageDigest

case class IndexFileHeader(version: Int, entryCount: Int)
case class IndexFileEntryStat(ctime: Date, mtime: Date, device: Int, inode: Int, mode: Int, uid: Int, gid: Int, size: Int)
case class IndexFileExtension(name: Seq[Byte], size: Int, data: Seq[Byte])
case class IndexFileEntry(stat: IndexFileEntryStat, id: ObjectId, flags: Seq[Byte], name: String)
case class IndexEntry(name: String, mode: Int = 429, stageLevel: StageLevel.StageLevel, id: ObjectId, status: Int)

case class Index(header: IndexFileHeader, entries: Seq[IndexEntry], extensions: Seq[IndexFileExtension]) {
  def length = entries.length
}

object Index {
  /** Returns the current repository status as new Index. */
  def status(repository: Repository): Index = Index.fromFile(new File(s"${repository.path}/index"))(repository)

  /** Adds the given path to the index. */
  def add(path: String)(repository: Repository): Unit = {
    val status = Index.status(repository)
    val newEntries = status.entries.map((entry: IndexEntry) => {
      if (entry.name == path) entry.copy(status = FileStatus.Staged) // TODO: Removed?
      else entry
    }).filter((entry) => FileStatus.isInStagingArea(entry.status))

    val updatedStatus = status.copy(entries = newEntries.toVector)

    Index.save(updatedStatus)(repository)
  }

  /** Removes the given path from the index. */
  def remove(path: String)(repository: Repository): Unit = {
    ???
  }

  /** Returns the status of the given file. */
  def retrieveFileStatus(file: File): Int = {
    ???
  }

  /** Saves the index to the 'index'-file. */
  private[git] def save(index: Index)(repository: Repository): Unit = {
    FileUtil.writeToFile(new File(s"${repository.path}/index"), Index.encode(index)(repository))
  }

  /** Returns a new Index constructed from the given file. */
  private[git] def fromFile(file: File)(repository: Repository): Index = {
    if (file.exists()) Index.decode(FileUtil.readContents(file))(repository)
    else Index(
      header = IndexFileHeader(version = 2, entryCount = 0),
      entries = getIndexEntries(Seq())(repository),
      extensions = Seq()
    )
  }

  /** Returns every index entry. */
  private[git] def getIndexEntries(indexFileEntries: Seq[IndexFileEntry])(repository: Repository): Seq[IndexEntry] = {
    val ignore = Ignore.fromPath(repository.wcPath)
    val ignoreDirectories = Vector(new File(PathUtil.combine(repository.wcPath, ".git")))

    FileUtil.recursiveListFiles(new File(repository.wcPath), ignoreDirectories = ignoreDirectories).collect{
      case file: File => indexFileEntries.find((e) => PathUtil.combine(repository.wcPath, e.name) == file.getAbsolutePath) match {
        case Some(IndexFileEntry(stat, id, flags, name)) => IndexEntry(name = name, stageLevel = StageLevel.Theirs, id = id, status = FileStatus.Current)

        // Not in the index file.
        case None => IndexEntry(
          name = PathUtil.relative(repository.wcPath, file.getPath),
          stageLevel = StageLevel.Ours,
          id = ObjectId.decode(ObjectDatabase.hashObject(Blob.decodeBody(FileUtil.readContents(file)))),
          status = if (Ignore.isIgnored(ignore, file)) FileStatus.Ignored else FileStatus.Untracked
        )
      }
    }
  }

  /** Returns the bytes decoded as an Index. */
  private[git] def decode(bytes: Seq[Byte])(repository: Repository): Index = {
    val reader = new DataReader(bytes)

    if (reader.takeString(4) != "DIRC") throw new CorruptRepositoryException("Corrupted Index file.")

    val header = IndexFileHeader(
      version = Conversion.bytesToInt(reader.take(4)),
      entryCount = Conversion.bytesToInt(reader.take(4))
    )

    // TODO: Support 3 and 4 versions.
    if (header.version > 2) throw new UnsupportedOperationException(s"Index file format version ${header.version} not supported.")

    /** Turns a two 4-byte lists into one `Date` object. */
    def createTime(seconds: Seq[Byte], nanoseconds: Seq[Byte]): Date = {
      val cal = new GregorianCalendar
      cal.setTimeInMillis(Conversion.bytesToInt(seconds) * 1000)
      cal.getTime
    }

    // Parse entries.
    val entryBuffer = Vector.newBuilder[IndexFileEntry]

    for (i <- 0 until header.entryCount) {
      // Construct the entry stat.
      val stat = IndexFileEntryStat(
        ctime = createTime(reader.take(4), reader.take(4)),
        mtime = createTime(reader.take(4), reader.take(4)),
        device = Conversion.bytesToInt(reader.take(4)),
        inode = Conversion.bytesToInt(reader.take(4)),
        mode = Conversion.bytesToInt(reader.take(4)), // TODO: Wrong.
        uid = Conversion.bytesToInt(reader.take(4)),
        gid = Conversion.bytesToInt(reader.take(4)),
        size = Conversion.bytesToInt(reader.take(4))
      )

      val id = reader.takeObjectId()

      val flags = reader.take(2)

      if (header.version > 2) {
        // TODO:
        val moreFlags = reader.take(2)
      }

      // Path name.
      val name = if (header.version < 4) {
        val name = reader.takeStringWhile(_ != 0)

        // Null padding 1-8 bytes, retaining the total size in multiple of eight.
        val total = if (62 + name.length % 8 == 0) 62 + name.length + 8 else 62 + name.length
        val padding = 8 - (total % 8)
        reader >> padding

        name
      } else {
        "" // TODO:
      }

      entryBuffer += IndexFileEntry(stat = stat, id = id, flags = flags, name = name)
    }

    // Extensions.
    val extensionBuffer = Vector.newBuilder[IndexFileExtension]

    while (reader.position < bytes.length - ObjectId.RawSize) {
      val signature = reader.take(4)
      val size = Conversion.bytesToInt(reader.take(4))
      val data = reader.take(size)

      // TODO: Create the signature.
      /*println(signature)
      println(size)
      println(new String(data.toArray))*/
    }

    // Checksum.
    val checksum = reader.takeObjectId()

    // TODO: Check checksum is ok.

    Index(
      header = header,
      entries = getIndexEntries(entryBuffer.result())(repository),
      extensions = extensionBuffer.result()
    )
  }

  /** Returns the index encoded as a sequence of bytes. */
  def encode(index: Index)(repository: Repository): Seq[Byte] = {
    val builder = Vector.newBuilder[Byte]

    // Header.
    builder ++= "DIRC"
    builder ++= Conversion.intToBytes(index.header.version)
    builder ++= Conversion.intToBytes(index.length)

    // Entries.
    index.entries.foreach((entry) => {
      val file = new File(PathUtil.combine(repository.wcPath, entry.name))
      val stat = FileUtil.stat(file)

      builder ++= Conversion.intToBytes(stat.ctime)
      builder ++= Conversion.intToBytes(stat.ctimeFractions)
      builder ++= Conversion.intToBytes(stat.mtime)
      builder ++= Conversion.intToBytes(stat.mtimeFractions)
      builder ++= Conversion.intToBytes(stat.device)
      builder ++= Conversion.intToBytes(stat.inode)
      builder ++= Conversion.intToBytes(stat.mode) // TODO: Wrong.
      builder ++= Conversion.intToBytes(stat.uid)
      builder ++= Conversion.intToBytes(stat.gid)
      builder ++= Conversion.intToBytes(stat.size)

      builder ++= entry.id

      val assumeValid = 0 // 1 << 15
      val extendedFlag = 0 // 1 << 14
      val stage = 0 // 0x1000, 0x2000, 0x3000
      val nameLength = if (entry.name.length < 0xfff) entry.name.length else 0xfff

      builder ++= Conversion.shortToBytes((0 | assumeValid | extendedFlag | stage | nameLength).toShort)

      // TODO: More flags.
      if (index.header.version > 2) {

      }

      if (index.header.version < 4) {
        builder ++= entry.name

        // Null padding 1-8 bytes, retaining the total size in multiple of eight.
        val total = if (62 + entry.name.length % 8 == 0) 62 + entry.name.length + 8 else 62 + entry.name.length
        val padding = 8 - (total % 8)

        builder ++= Vector.fill(padding)(0)
      } else {
        // TODO:
      }
    })

    // Extensions.
    index.extensions.foreach((extension) => {
      // TODO: Write extensions.
    })

    // Checksum.
    val digest = MessageDigest.getInstance("SHA-1")
    builder ++= digest.digest(builder.result().toArray)

    builder.result()
  }
}

object StageLevel extends Enumeration {
  type StageLevel = Value

  val Staged = Value("staged")
  val Ancestor = Value("ancestor")
  val Ours = Value("ours")
  val Theirs = Value("theirs")
}