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

import scala.io.Source
import git.util.{PathUtil}
import java.io.File
import scala.annotation.tailrec

case class Ignore(entries: List[String], root: File) {
  //@tailrec TODO: tailrec
  def isIgnored(file: File): Boolean = {
    if (file.getPath == ".gitignore") true
    else {
      val ignored = entries.exists(Ignore.matches(_, file, root))

      // Recursively check if the parent is ignored.
      if (file.getParentFile.compareTo(root) > 0) {
        // It was already ignored, skip checking for parent folders.
        if (ignored) true
        else isIgnored(file.getParentFile)
      }
      else ignored
    }
  }
}

object Ignore {
  def fromPath(wcPath: String): Ignore = {
    Ignore(entries = Source.fromFile(s"$wcPath/.gitignore").getLines().toList, root = new File(wcPath))
  }

  private def regexFromGlob(glob: String) = {
    val out = new StringBuffer

    out.append(".*")

    for (i <- glob) i match {
      case '*' => out.append("[^/]*")
      case '?' => out.append(".")
      case '.' => out.append("\\.")
      case '\\' => out.append("\\\\")
      case _ => out.append(i)
    }

    out.append("$")

    out.toString
  }

  /** Tries to match the pattern against the given path. Empty patterns do not match. */
  private def matches(pattern: String, file: File, root: File): Boolean = {
    if (pattern.startsWith("#") || pattern.isEmpty) false
    else {
      val matches = {
        // Unix-normalized paths.
        val filePath = file.getPath.replaceAllLiterally("\\", "/")
        val rootPath = root.getPath.replaceAllLiterally("\\", "/")
        val patternPath = PathUtil.combine(rootPath, pattern).replaceAllLiterally("\\", "/")
        val patternSimplePath = pattern.replaceAll("/$", "")

        if (pattern.endsWith("/")) {
          // We only ignore directories.
          if (!file.isDirectory) false
          else filePath.matches(Ignore.regexFromGlob(patternSimplePath))
        } else if (!pattern.contains("/")) {
          // If the pattern has no /, then match against the filename instead of the path.
          file.getName.matches(Ignore.regexFromGlob(pattern))
        } else {
          filePath.matches(Ignore.regexFromGlob(patternPath))
        }
      }

      // TODO: Patterns can be negated with !.
      //if (pattern.startsWith("!")) !matches else matches

      matches
    }
  }
}