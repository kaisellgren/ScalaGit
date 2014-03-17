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

import java.io.File
import git.util.FileUtil
import scala.Some

case class Repository(
  path: String,
  wcPath: String,
  cache: Cache = Cache()
)

object Repository {
  private[git] def head(repository: Repository) = {
    val refs = Reference.find(repository)

    refs.head match {
      case None => None
      case Some(head) => Branch.find(repository).find(_.tipId == head.targetIdentifier) match {
        case a: Some[BaseBranch] => Some(a.x)
        case _ => Some(DetachedHead(tipId = head.targetIdentifier))
      }
    }
  }

  private[git] def open(path: String): Repository = {
    val workingCopyPath = path.replace(".git", "")
    val repositoryPath = workingCopyPath + "/.git"

    initializeRepository(repositoryPath)

    Repository(
      path = repositoryPath,
      wcPath = workingCopyPath
    )
  }

  private def isInitialized(path: String): Boolean = new File(path + "/HEAD").exists()

  private def initializeRepository(path: String) {
    // If this repository does not exist (user wishes to create a new one), then set up the remaining files.
    if (!isInitialized(path)) {
      // Always ensure we have the basic folder structure.
      new File(path).mkdirs()

      new File(s"$path/objects/pack").mkdirs()
      new File(s"$path/objects/info").mkdirs()
      new File(s"$path/refs/heads").mkdirs()
      new File(s"$path/refs/tags").mkdirs()
      new File(s"$path/refs/notes").mkdirs()
      new File(s"$path/refs/remotes").mkdirs()

      FileUtil.createFileWithContents(s"$path/description", "Unnamed repository; edit this file 'description' to name the repository.\n")
      FileUtil.createFileWithContents(s"$path/HEAD", "ref: refs/heads/master\n")

      // TODO: Let's implement a Config class.
      FileUtil.createFileWithContents(s"$path/config", "[core]\n\trepositoryformatversion = 0\n\tfilemode = false\n\tbare = false\n\tlogallrefupdates = true\n\tsymlinks = false\n\tignorecase = true\n\thideDotFiles = dotGitOnly")
    }
  }
}