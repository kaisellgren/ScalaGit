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

import git.util.Conversion

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

  def fromBytes(bytes: Seq[Byte]) = ObjectId(Conversion.bytesToHexString(bytes))

  /** Constructs ObjectId from plain byte sha. It strips the whitespace from the end if any. */
  def fromPlain(bytes: Seq[Byte]) = ObjectId(new String(bytes.take(40).toList))
}