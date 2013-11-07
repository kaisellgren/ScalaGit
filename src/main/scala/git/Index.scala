package git

import git.util.FileUtil
import java.io.File
import scala.collection.mutable.ListBuffer

case class IndexEntry(
  name: String,
  mode: Int = 0655,
  stageLevel: StageLevel.StageLevel,
  id: ObjectId,
  status: Int
)

case class Index(entries: List[IndexEntry]) {
  def length = entries.length
}

object Index {
  def retrieveFileStatus(file: File): Int = {
    FileStatus.Current
  }

  def fromFile(file: File, wcPath: String): Index = {
    val indexFile = IndexFile.fromBytes(FileUtil.readContents(file))
    val ignore = Ignore.fromPath(wcPath)

    val entries = FileUtil.recursiveListFiles(new File(wcPath)).collect{
      case file: File => indexFile.entries.find(_.name == file.getName) match {
        case Some(IndexFileEntry(stat, id, flags, name, padding)) => IndexEntry(name = name, stageLevel = StageLevel.Theirs, id = id, status = FileStatus.Current)

        // Not in the index file.
        case None => IndexEntry(
          name = file.getName,
          stageLevel = StageLevel.Ours,
          id = ObjectId("asd"),
          status = if (ignore.isIgnored(file.getName)) FileStatus.Ignored else FileStatus.Untracked
        )
      }
    }

    entries.foreach((f) => {
      if ((f.status & FileStatus.Untracked) != 0) println(f.name)
    })

    Index(entries = Nil)
  }
}

object StageLevel extends Enumeration {
  type StageLevel = Value

  val Staged = Value("staged")
  val Ancestor = Value("ancestor")
  val Ours = Value("ours")
  val Theirs = Value("theirs")
}