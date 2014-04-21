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

case class Blob(
  override val id: ObjectId,
  override val header: ObjectHeader,
  size: Int = 0,
  contents: Seq[Byte]
) extends Object

object Blob {
  /** Returns the blob encoded as a sequence of bytes. */
  private[git] def encode(blob: Blob): Seq[Byte] = {
    val builder = Vector.newBuilder[Byte]

    val body = Blob.encodeBody(blob)
    val header = if (blob.header.length > 0) blob.header else blob.header.copy(length = body.length)

    builder ++= header
    builder ++= body

    builder.result()
  }

  /** Returns the blob body encoded as a sequence of bytes. */
  private[git] def encodeBody(blob: Blob): Seq[Byte] = blob.contents

  /** Returns the bytes decoded as a Blob body. */
  private[git] def decodeBody(bytes: Seq[Byte], id: Option[ObjectId] = None, header: Option[ObjectHeader] = None): Blob = {
    val blob = Blob(
      id = id.getOrElse(ObjectId("")),
      header = header.getOrElse(ObjectHeader(ObjectType.Blob)),
      size = bytes.length,
      contents = bytes
    )

    id match {
      case Some(value) => blob
      case None => blob.copy(
        id = ObjectId.decode(ObjectDatabase.hashObject(Blob.encode(blob)))
      )
    }
  }

  /** Returns the bytes decoded as a Blob. */
  private[git] def decode(bytes: Seq[Byte]): Blob = {
    val header = ObjectHeader.decode(bytes)
    val data = bytes.takeRight(header.length)

    decodeBody(data, id = None, header = Some(header))
  }

  /** Returns the blob for the given ID as [[Option]]. */
  def findById(id: ObjectId)(repository: Repository): Option[Blob] = ObjectDatabase.findObjectById(id)(repository) match {
    case Some(blob: Blob) => Some(blob)
    case _ => None
  }
}