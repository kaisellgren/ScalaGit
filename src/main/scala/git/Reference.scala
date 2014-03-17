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

import java.io.File
import scala.io.Source

// remote: Remote // TODO: Implement remotes.
case class Reference(canonicalName: String, targetIdentifier: ObjectId, remoteName: Option[String] = None)

object Reference {
  val LocalBranchPrefix = "/refs/heads/"
  val RemoteTrackingBranchPrefix = "/refs/remotes/"
  val TagPrefix = "/refs/tags/"
  val NotePrefix = "/refs/notes/"

  private[git] def find(repository: Repository): ReferenceCollection = {
    // Create references based on the refs folder.
    val localReferences = new File(repository.path + Reference.LocalBranchPrefix).listFiles.map(file => Reference(file.getName, ObjectId(Source.fromFile(file).mkString.trim))).toList

    // TODO: Improve.
    val buffer = Vector.newBuilder[Reference]
    new File(repository.path + Reference.RemoteTrackingBranchPrefix).listFiles.foreach((folder: File) => {
      if (folder.isDirectory) {
        folder.listFiles.foreach((file: File) => {
          val ref = Reference(file.getName, ObjectId(Source.fromFile(file).mkString.trim), remoteName = Some(folder.getName))

          if (file.getName != "HEAD") buffer += ref
        })
      }
    })
    val remoteReferences = buffer.result()

    // Create the HEAD reference. Read the HEAD file and figure out the canonical name.
    val regex = s"""(?s).*refs/heads/([a-zA-Z0-9]+).*""".r
    val canonicalName = regex.findFirstMatchIn(Source.fromFile(repository.path + "/HEAD").mkString).get.group(1)
    val head = localReferences.find((r: Reference) => r.canonicalName == canonicalName)

    ReferenceCollection(head = head, localReferences = localReferences, remoteReferences = remoteReferences)
  }
}