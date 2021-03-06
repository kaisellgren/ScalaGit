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

/** The base Git object. */
trait Object {
  def id: ObjectId
  def header: ObjectHeader

  override def equals(o: Any) = o match {
    case that: Object => that.id.sha == id.sha
    case _ => false
  }

  override def hashCode = id.sha.hashCode
}

object Object {
  def encode(o: Object): Seq[Byte] = o match {
    case o: Commit => Commit.encode(o)
    case o: Tag => Tag.encode(o)
    case o: Blob => Blob.encode(o)
    case o: Tree => Tree.encode(o)
    //case o: Note => Note.toObjectFile(o)
  }
}