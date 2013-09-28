package git

class TreeEntry {
  var mode: Int = _
  var name: String = _
  var id: ObjectId = _

  override def toString = s"TreeEntry(mode: $mode, name: $name, id: $id)"
}
