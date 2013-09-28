package git

class ObjectId {
  var sha: String = _

  override def toString = s"ObjectId($sha)"
}

object ObjectId {
  val RawSize = 20
  val HexSize = 40

  def apply(id: String) = ObjectId.fromHash(id)

  def fromHash(id: String): ObjectId = {
    val o = new ObjectId
    o.sha = id
    o
  }

  def fromBytes(bytes: Iterable[Byte]): ObjectId = {
    val o = new ObjectId
    o.sha = bytes.map("%02x".format(_)).mkString
    o
  }
}