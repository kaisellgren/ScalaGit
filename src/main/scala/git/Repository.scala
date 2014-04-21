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

case class Repository(path: String, wcPath: String, cache: Cache = Cache())

object Repository {
  /** Initializes the repository and returns the new instance. */
  def open(path: String): Repository = {
    val workingCopyPath = path.replace(".git", "")
    val repositoryPath = workingCopyPath + "/.git"

    initializeRepository(repositoryPath)

    Repository(path = repositoryPath, wcPath = workingCopyPath)
  }

  /** Returns true if the given repository has been initialized. */
  private[this] def isInitialized(path: String): Boolean = new File(path + "/HEAD").exists()

  /** Initializes the repository. Creates the necessary folder structure and files. */
  private[this] def initializeRepository(path: String) {
    // Always ensure we have the basic folder structure.
    val paths = Seq(s"$path/objects/pack", s"$path/objects/info", s"$path/refs/tags", s"$path/refs/notes", s"$path/refs/remotes")
    paths.foreach((path) => new File(path).mkdirs())

    // If this repository does not exist (user wishes to create a new one), then set up the remaining files.
    if (!isInitialized(path)) {
      FileUtil.createFileWithContents(s"$path/description", "Unnamed repository; edit this file 'description' to name the repository.\n")
      FileUtil.createFileWithContents(s"$path/HEAD", "ref: refs/heads/master\n")

      // TODO: Let's implement a Config class or something.
      FileUtil.createFileWithContents(s"$path/config", "[core]\n\trepositoryformatversion = 0\n\tfilemode = false\n\tbare = false\n\tlogallrefupdates = true\n\tsymlinks = false\n\tignorecase = true\n\thideDotFiles = dotGitOnly")
    }
  }

  /** Returns the repository head. This is usually a [[Branch]], but can also be [[DetachedHead]]. */
  def head(repository: Repository): Option[BaseBranch] = {
    Reference.find(repository).head match {
      case None => None
      case Some(head) => Branch.find(repository).find(_.tipId == head.targetIdentifier) match {
        case Some(a: BaseBranch) => Some(a)
        case _ => Some(DetachedHead(tipId = head.targetIdentifier))
      }
    }
  }

  /** Returns the repository head as an optional commit. */
  def headAsCommit(repository: Repository): Option[Commit] = {
    Repository.head(repository) match {
      case None => None
      case Some(branch) => Some(Branch.tip(branch)(repository))
    }
  }
}