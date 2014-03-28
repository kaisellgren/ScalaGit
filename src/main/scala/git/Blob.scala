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
  def decode(bytes: Seq[Byte], id: Option[ObjectId] = None): Blob = {
    val blob = Blob(
      id = id match {
        case Some(v) => v
        case None => ObjectId("")
      },
      header = ObjectHeader(ObjectType.Blob, length = bytes.length),
      size = bytes.length,
      contents = bytes
    )

    if (id.isDefined) blob
    else blob.copy(id = ObjectId.fromBytes(ObjectDatabase.hashObject(Blob.encode(blob))))
  }

  def encode(blob: Blob): Seq[Byte] = {
    val builder = Vector.newBuilder[Byte]

    builder ++= blob.header
    builder ++= blob.contents

    builder.result()
  }

  def findById(id: ObjectId)(repository: Repository): Option[Blob] = ObjectDatabase.findObjectById(id)(repository) match {
    case Some(blob: Blob) => Some(blob)
    case _ => None
  }
}