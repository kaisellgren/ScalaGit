package git

import scala.collection._
import scala.collection.generic._

class CommitLog(val repository: Repository) extends Traversable[Commit] {
  def find(filter: CommitFilter): Unit = {
    // TODO: We assume a lot here regarding the filter... to get us started.
    val beginningIdentifier = repository.head.targetIdentifier
    val r = repository.database.findObjectById(repository.head.targetIdentifier)
    println(r)
    List()
  }

  def foreach[T](f: Commit => T): Unit = {

  }
}