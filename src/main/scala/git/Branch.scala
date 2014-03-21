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

sealed trait BaseBranch {
  def trackedBranch: Option[BaseBranch]
  def tipId: ObjectId
  def name: String
  def canonicalName: String
  def isRemote: Boolean = false

  def isTracking: Boolean = !trackedBranch.isEmpty
}

case class Branch(
  trackedBranch: Option[BaseBranch],
  tipId: ObjectId,
  name: String,
  canonicalName: String,
  override val isRemote: Boolean = false
) extends BaseBranch

case class DetachedHead(tipId: ObjectId) extends BaseBranch {
  def name = "(no branch)"
  def canonicalName = "(no branch)"
  def trackedBranch = None
}

object Branch {
  def find(repository: Repository): Seq[Branch] = {
    val buffer = Vector.newBuilder[Branch]

    val refs = Reference.find(repository)

    // Construct branches.
    refs.references.foreach((r) => {
      val isRemote = refs.remoteReferences.contains(r)

      buffer += Branch(
        tipId = r.targetIdentifier,
        name = r.canonicalName,
        isRemote = isRemote,
        canonicalName = if (isRemote) s"remotes/${r.remoteName.get}/${r.canonicalName}" else s"origin/${r.canonicalName}",
        trackedBranch = None // TODO: Implement for isRemote
      )
    })

    buffer.result()
  }

  def tip(branch: BaseBranch)(repository: Repository): Commit = ObjectDatabase.findObjectById(repository, branch.tipId) match {
    case Some(o: Commit) => o
    case _ => throw new CorruptRepositoryException(s"Could not find the commit the branch ${branch.name} points to.")
  }

  def commits(branch: BaseBranch)(repository: Repository): Seq[Commit] = Commit.find(CommitFilter(since = Some(List(branch))))(repository)
}