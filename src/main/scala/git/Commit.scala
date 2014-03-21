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
import com.sun.javaws.exceptions.InvalidArgumentException
import scala.annotation.tailrec

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
  def find(filter: CommitFilter = CommitFilter())(repository: Repository): Seq[Commit] = {
    val buffer = Vector.newBuilder[Commit]

    // Prepare the "since" value.
    val sinceIds = filter.since match {
      // Defaults to HEAD.
      case None => Repository.head(repository) match {
        case None => throw new NoHeadException("No HEAD has been set and you queried for commits using HEAD as the 'since' value of the filter.")
        case Some(head: BaseBranch) => List(Branch.tip(head)(repository).id)
      }

      case Some(list) => list.map{
        case id: ObjectId => id
        case b: Branch => Branch.tip(b)(repository).id
        case _ => throw new InvalidArgumentException(Array("Invalid commit filter: you passed an invalid object as part of 'since'."))
      }
    }

    // Helper that finds a commit or throws.
    def findCommit(id: ObjectId): Commit = Commit.findById(id)(repository).getOrElse(throw CorruptRepositoryException(s"Could not find commit $id."))

    // By most recent.
    if (filter.sort == CommitSortStrategy.Time) {
      @tailrec
      def iterate(commits: Seq[Commit], acc: Int) {
        if (commits.length != 0) {
          val mostRecent = commits.maxBy(_.commitDate)

          buffer += mostRecent

          // Have we hit the limit?
          if (acc + 1 < filter.limit) {
            // Let's come up with a new set of commits. The next 'most recent'.
            val nextCommits = Set.newBuilder[Commit]

            // For most recent, add its parents.
            nextCommits ++= mostRecent.parentIds.map(findCommit)

            // Add every commit except the most recent.
            nextCommits ++= commits.filter(_ != mostRecent)

            iterate(nextCommits.result().toVector, acc + 1)
          }
        }
      }

      iterate(sinceIds.map(findCommit), 0)

      buffer.result()
    } else {
      buffer.result().toVector
    }
  }

  def find(repository: Repository): Seq[Commit] = find()(repository)

  def findById(id: ObjectId)(repository: Repository): Option[Commit] = ObjectDatabase.findObjectById(repository, id) match {
    case Some(c: Commit) => Some(c)
    case _ => None
  }

  def tree(commit: Commit)(repository: Repository): Tree = ObjectDatabase.findObjectById(repository, commit.treeId) match {
    case Some(o: Tree) => o
    case _ => throw new CorruptRepositoryException(s"Could not find the tree for the commit (${commit.id}).")
  }

  def parents(commit: Commit)(repository: Repository): Seq[Commit] = commit.parentIds.map((id: ObjectId) => {
    Commit.findById(id)(repository) match {
      case Some(c: Commit) => c
      case None => throw new CorruptRepositoryException(s"Could not find commit $id")
    }
  })

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

    if (reader.takeString(5) != "tree ") throw new CorruptRepositoryException("Corrupted Commit object file.")

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

    if (reader.takeString(7) != "author ") throw new CorruptRepositoryException("Corrupted Commit object file.")

    val author = parseUserFields(reader)

    if (reader.takeString(10) != "committer ") throw new CorruptRepositoryException("Corrupted Commit object file.")

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