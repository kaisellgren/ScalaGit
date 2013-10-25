package git

case class ObjectId(sha: String) {
  override def equals(o: Any) = o match {
    case that: ObjectId => that.sha == sha
    case _ => false
  }

  override def hashCode = sha.hashCode
}

object ObjectId {
  val RawSize = 20
  val HexSize = 40

  def fromBytes(bytes: List[Byte]) = ObjectId(bytes.map("%02x".format(_)).mkString)
}