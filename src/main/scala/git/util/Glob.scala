package git.util

object Glob {
  private def regexFromGlob(glob: String) = {
    val out = new StringBuffer

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

  def matches(pattern: String, path: String): Boolean = {
    if (pattern.startsWith("#") || pattern.isEmpty) false
    else {
      val negated = pattern.startsWith("!")

      // Directory matching.
      if (pattern.endsWith("/")) {
        path.contains(pattern)
      }

      // Simple glob match.
      else if (!pattern.contains("/")) {
        path.matches(regexFromGlob(pattern))
      } else if (pattern.startsWith("/")) {
        val pathParts = path.split('/')
        val patternParts = pattern.split('/')

        // There can't be less path parts than pattern parts for a match.
        if (patternParts.length > pathParts.length) false
        else {
          patternParts.forall((p) => pathParts(patternParts.indexOf(p)).matches(regexFromGlob(p)))
        }
      } else {
        path.matches(regexFromGlob(pattern))
      }
    }
  }
}
