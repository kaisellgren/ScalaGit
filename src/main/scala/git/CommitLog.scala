package git

import scala.collection._
import scala.collection.generic._
import scala.collection.mutable.ListBuffer

class CommitLog(val repository: Repository) extends Traversable[Commit] {
  def find(filter: CommitFilter): List[Commit] = {
    var buffer = new ListBuffer[Commit]

    // Prepare the "since" value.
    val sinceIds = filter.since match {
      // Defaults to HEAD.
      case None => repository.head() match {
        case None => throw new Exception("No HEAD has been set and you queried for commits using HEAD as the 'since' value of the filter.")
        case Some(head) => List(head.tip().id)
      }

      case Some(list) => list.map{
        case a: ObjectId => a
        case b: Branch => b.tip().id
        case _ => throw new Exception("Invalid commit filter: you passed an invalid object as part of 'since'.")
      }
    }

    if (filter.sort == CommitSortStrategy.Time) {
      // Fill buffer with commits from all "since" sources.
      sinceIds.foreach((sinceId: ObjectId) => {
        def findNSinceId(n: Int, id: ObjectId) {
          repository.database.findObjectById(id).get match { // TODO: Handle when this ends (None).
            case commit: Commit => {
              if (commit.id != null && !buffer.contains(commit)) buffer += commit
              if (n > 1) commit.parentIds.foreach(findNSinceId(n -1, _))
            }
          }
        }

        findNSinceId(filter.limit, sinceId)
      })

      buffer = buffer.sortBy(_.commitDate).take(filter.limit)
    }

    buffer.toList
  }

  def foreach[T](f: Commit => T): Unit = {

  }
}