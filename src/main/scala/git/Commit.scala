package git

class Commit {
  var message: String = _

  override def toString = s"Commit(message: $message)"
}