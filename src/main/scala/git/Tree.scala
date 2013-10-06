package git

import scala.collection.mutable.ListBuffer

class Tree extends Object {
  var entries: List[TreeEntry] = _
}

object Tree {
  def fromObjectFile(bytes: Array[Short]): Tree = {
    val o = new Tree

    var data = bytes.drop(0)

    val entryBuilder = new ListBuffer[TreeEntry]

    def parseEntry() {
      val entry = new TreeEntry

      val modeBytes = data.takeWhile(_ != 32)
      entry.mode = new String(modeBytes).toInt

      data = data.drop(modeBytes.length + 1)

      val nameBytes = data.takeWhile(_ != 0)
      entry.name = new String(nameBytes)

      data = data.drop(nameBytes.length + 1)

      entry.id = ObjectId.fromBytes(data.take(ObjectId.RawSize))

      data = data.drop(ObjectId.RawSize)

      entryBuilder += entry
    }

    parseEntry()

    o.entries = entryBuilder.toList

    o
  }
}
