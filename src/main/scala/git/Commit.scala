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
  /** Returns commits based on the given filters. */
  def find(filter: CommitFilter = CommitFilter())(repository: Repository): Seq[Commit] = {
    val buffer = Vector.newBuilder[Commit]

    // Prepare the "since" value.
    val sinceIds = filter.since match {
      // Defaults to HEAD.
      case None => Repository.head(repository) match {
        case None => throw new NoHeadException("No HEAD has been set and you queried for commits using HEAD as the 'since' value of the filter.")
        case Some(head: BaseBranch) => List(Branch.tip(head)(repository).id)
      }

      case Some(items) => items.map {
        case id: ObjectId => id
        case b: Branch => Branch.tip(b)(repository).id
        case _ => throw new Exception("Invalid commit filter: you passed an invalid object as part of 'since'.")
      }
    }

    // Helper that finds a commit or throws.
    def findCommit(id: ObjectId): Commit = Commit.findById(id)(repository).getOrElse(throw CorruptRepositoryException(s"Could not find commit $id."))

    // By most recent.
    filter.sort match {
      // Find commits based on the order of time.
      case CommitSortStrategy.Time =>
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

        iterate(sinceIds.map(findCommit).toVector, 0)

        buffer.result()
      case _ => ???
    }
  }

  /** Returns commits based on default criteria. */
  def find(repository: Repository): Seq[Commit] = find()(repository)

  /** Returns the commit for the given ID. */
  def findById(id: ObjectId)(repository: Repository): Option[Commit] = ObjectDatabase.findObjectById(id)(repository) match {
    case Some(c: Commit) => Some(c)
    case _ => None
  }

  /** Returns the tree for the given commit. */
  def tree(commit: Commit)(repository: Repository): Tree = ObjectDatabase.findObjectById(commit.treeId)(repository) match {
    case Some(o: Tree) => o
    case _ => throw new CorruptRepositoryException(s"Could not find the tree for the commit (${commit.id}).")
  }

  /** Returns the parent commits for the given commit. */
  def parents(commit: Commit)(repository: Repository): Seq[Commit] = commit.parentIds.map((id: ObjectId) => {
    Commit.findById(id)(repository) match {
      case Some(c: Commit) => c
      case None => throw new CorruptRepositoryException(s"Could not find commit $id")
    }
  }).toVector

  /** Returns the commit encoded as a sequence of bytes. */
  private[git] def encode(commit: Commit): Seq[Byte] = {
    val builder = Vector.newBuilder[Byte]

    val body = Commit.encodeBody(commit)
    val header = if (commit.header.length > 0) commit.header else commit.header.copy(length = body.length)

    builder ++= header
    builder ++= body

    builder.result()
  }

  /** Returns the commit body encoded as a sequence of bytes. */
  private[git] def encodeBody(commit: Commit): Seq[Byte] = {
    val buffer = Vector.newBuilder[Byte]

    buffer ++= s"tree ${commit.treeId.sha}\n"

    commit.parentIds.foreach(id => {
      buffer ++= s"parent ${id.sha}\n"
    })

    buffer ++= s"author ${commit.authorName} <${commit.authorEmail}> ${dateToGitFormat(commit.authorDate)}\n"
    buffer ++= s"committer ${commit.committerName} <${commit.committerEmail}> ${dateToGitFormat(commit.commitDate)}\n"

    buffer ++= s"\n${commit.message}"

    buffer.result()
  }

  /** Returns the bytes decoded as a Commit. */
  private[git] def decode(bytes: Seq[Byte]): Commit = {
    val header = ObjectHeader.decode(bytes)
    val data = bytes.takeRight(header.length)

    decodeBody(data, id = None, header = Some(header))
  }

  /** Returns the bytes decoded as a Commit body. */
  private[git] def decodeBody(bytes: Seq[Byte], id: Option[ObjectId] = None, header: Option[ObjectHeader] = None): Commit = {
    val reader = new DataReader(bytes)

    if (reader.takeString(5) != "tree ") throw new CorruptRepositoryException("Corrupted Commit object file.")

    // Followed by tree hash.
    val treeId = reader.takeStringBasedObjectId()

    reader >> 1 // LF.

    val parentIdsBuilder = Vector.newBuilder[ObjectId]

    // What follows is 0-n number of parent references.
    def parseParentIds() {
      // Stop if the data does not begin with "parent".
      if (reader.takeStringWhile(_ != ' ') == "parent") {
        reader >> 1 // Space.

        parentIdsBuilder += reader.takeStringBasedObjectId()

        reader >> 1 // LF.

        parseParentIds()
      }
    }

    parseParentIds()

    val parentIds = parentIdsBuilder.result()

    reader << 6 // The parent ID parsing goes 6 bytes too far ("parent").

    if (reader.takeString(7) != "author ") throw new CorruptRepositoryException("Corrupted Commit object file.")

    val author = parseUserFields(reader)

    if (reader.takeString(10) != "committer ") throw new CorruptRepositoryException("Corrupted Commit object file.")

    val committer = parseUserFields(reader)

    // Finally the commit message.
    val message = new String(reader.getRest.toList).trim

    val commit = Commit(
      id = id.getOrElse(ObjectId("")),
      header = header.getOrElse(ObjectHeader(ObjectType.Commit)),
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

    if (id.isDefined) commit
    else commit.copy(id = ObjectId.decode(ObjectDatabase.hashObject(Commit.encode(commit))))
  }
}