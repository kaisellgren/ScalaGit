/*
 * Copyright (c) 2014 the original author or authors.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package git

import java.util.Date
import git.util.Parser._
import git.util.DataReader
import scala.collection.mutable.ListBuffer

case class Commit(
  override val id: ObjectId,
  override val header: ObjectHeader,
  authorName: String,
  authorEmail: String,
  authorDate: Date,
  committerName: String,
  committerEmail: String,
  commitDate: Date,
  message: String,
  treeId: ObjectId,
  parentIds: Seq[ObjectId]
) extends Object

object Commit {
  def find(filter: Option[CommitFilter])(repository: Repository): Seq[Commit] = {
    val buffer = Vector.newBuilder[Commit]

    // Prepare the "since" value.
    /*val sinceIds = filter.since match {
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
    }*/

    buffer.result()

    ???
  }

  def find(repository: Repository): Seq[Commit] = find(filter = None)(repository)

  def tree(commit: Commit)(repository: Repository): Tree = ObjectDatabase.findObjectById(repository, commit.treeId) match {
    case Some(o: Tree) => o
    case _ => throw new Exception(s"Could not find the tree for the commit (${commit.id}).")
  }

  def parents(): Seq[Commit] = ???

  def toObjectFile(commit: Commit): Seq[Byte] = {
    val buffer = new ListBuffer[Byte]

    // TODO: Where do we create the ID?

    buffer.appendAll(s"tree ${commit.treeId.sha}\n".getBytes)

    commit.parentIds.foreach(id => {
      buffer.appendAll(s"parent ${id.sha}\n".getBytes)
    })

    buffer.appendAll(s"author ${commit.authorName} <${commit.authorEmail}> ${dateToGitFormat(commit.authorDate)}\n".getBytes)
    buffer.appendAll(s"committer ${commit.committerName} <${commit.committerEmail}> ${dateToGitFormat(commit.commitDate)}\n".getBytes)

    buffer.appendAll(s"\n${commit.message}".getBytes)

    // Insert the header in the beginning.
    buffer.insertAll(0, ObjectHeader(typ = ObjectType.Commit, length = buffer.length))

    buffer.toList
  }

  def fromObjectFile(bytes: Seq[Byte], repository: Repository, id: ObjectId, header: Option[ObjectHeader]): Commit = {
    val reader = new DataReader(bytes)

    if (reader.takeString(5) != "tree ") throw new Exception("Corrupted Commit object file.")

    // Followed by tree hash.
    val treeId = reader.takeStringBasedObjectId()

    reader >> 1 // LF.

    val parentIdsBuffer = new ListBuffer[ObjectId]

    // What follows is 0-n number of parent references.
    def parseParentIds() {
      // Stop if the data does not begin with "parent".
      if (reader.takeStringWhile(_ != ' ') == "parent") {
        reader >> 1 // Space.

        parentIdsBuffer += reader.takeStringBasedObjectId()

        reader >> 1 // LF.

        parseParentIds()
      }
    }

    parseParentIds()

    val parentIds = parentIdsBuffer.toList

    reader << 6 // The parent ID parsing goes 6 bytes too far ("parent").

    if (reader.takeString(7) != "author ") throw new Exception("Corrupted Commit object file.")

    val author = parseUserFields(reader)

    if (reader.takeString(10) != "committer ") throw new Exception("Corrupted Commit object file.")

    val committer = parseUserFields(reader)

    // Finally the commit message.
    val message = new String(reader.getRest.toList).trim

    Commit(
      id = id,
      header = header match {
        case Some(v) => v
        case None => ObjectHeader(ObjectType.Commit)
      },
      authorName = author.name,
      authorEmail = author.email,
      authorDate = author.date,
      committerName = committer.name,
      committerEmail = committer.email,
      commitDate = committer.date,
      message = message,
      treeId = treeId,
      parentIds = parentIds
    )
  }
}