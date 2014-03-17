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
  contents: Seq[Byte] // TODO: We have to make sure we don't unnecessarily create blobs and consume memory.
) extends Object

object Blob {
  def fromObjectFile(bytes: Seq[Byte], id: ObjectId, repository: Repository, header: Option[ObjectHeader]): Blob = Blob(
    id = id,
    header = header match {
      case Some(v) => v
      case None => ObjectHeader(ObjectType.Blob)
    },
    size = bytes.length,
    contents = bytes
  )

  def toObjectFile(blob: Blob) = ???
}