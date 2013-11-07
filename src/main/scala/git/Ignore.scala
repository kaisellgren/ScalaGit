package git

import scala.io.Source
import git.util.Glob

case class Ignore(entries: List[String]) {
  def isIgnored(path: String): Boolean = entries.exists(Glob.matches(_, path))
}

object Ignore {
  def fromPath(wcPath: String): Ignore = {
    Ignore(entries = Source.fromFile(s"$wcPath/.gitignore").getLines().toList)
  }
}