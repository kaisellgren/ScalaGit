package git

class Blob extends Object {
  var size = 0
  var contents: List[Short] = _ // TODO: Let's not load the data in memory.

  def isBinary: Boolean = {
    // TODO: Try to find at least one null byte.
    false
  }
}

object Blob {
  def fromObjectFile(bytes: Array[Short]): Blob = {
    val o = new Blob

    o.contents = bytes.toList
    o.size = bytes.length

    o
  }
}