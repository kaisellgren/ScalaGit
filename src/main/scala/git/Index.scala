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

import git.util.{PathUtil, FileUtil}
import java.io.File

case class IndexEntry(
  name: String,
  mode: Int = 0655,
  stageLevel: StageLevel.StageLevel,
  id: ObjectId,
  status: Int
)

case class Index(entries: Seq[IndexEntry]) {
  def length = entries.length
}

object Index {
  def status(repository: Repository): Index = Index.fromFile(new File(s"${repository.path}/index"), wcPath = repository.wcPath)

  def retrieveFileStatus(file: File): Int = {
    FileStatus.Current
    ???
  }

  def fromFile(file: File, wcPath: String): Index = {
    val indexFile = IndexFile.fromBytes(FileUtil.readContents(file))
    val ignore = Ignore.fromPath(wcPath)
    val ignoreDirectories = Some(List(new File(PathUtil.combine(wcPath, ".git"))))

    val entries = FileUtil.recursiveListFiles(new File(wcPath), ignoreDirectories = ignoreDirectories).collect{
      case file: File => indexFile.entries.find((e) => PathUtil.combine(wcPath, e.name) == file.getAbsolutePath) match {
        case Some(IndexFileEntry(stat, id, flags, name, padding)) => IndexEntry(name = name, stageLevel = StageLevel.Theirs, id = id, status = FileStatus.Current)

        // Not in the index file.
        case None => IndexEntry(
          name = file.getPath,
          stageLevel = StageLevel.Ours,
          id = ObjectId(""),
          status = if (ignore.isIgnored(file)) FileStatus.Ignored else FileStatus.Untracked
        )
      }
    }

    entries.foreach((f) => {
      if (FileStatus.isUntracked(f.status)) println(f.name)
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