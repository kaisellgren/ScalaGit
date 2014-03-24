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

import git.ObjectType.ObjectType
import scala.collection.mutable.ListBuffer

case class ObjectHeader(typ: ObjectType, length: Int = 0)

object ObjectHeader {
  def encode(header: ObjectHeader): Seq[Byte] = {
    val buffer = new ListBuffer[Byte]

    buffer.appendAll(s"${header.typ} ".getBytes("US-ASCII"))
    buffer.appendAll(s"${header.length}".getBytes("US-ASCII"))
    buffer.append(0)

    buffer.toList
  }

  def decode(bytes: Seq[Byte]): ObjectHeader = {
    // Figure out the object type. Read until we hit a space.
    val typeData = bytes.takeWhile(_ != 32)
    val t = ObjectType.withName(new String(typeData.toList))

    val lengthData = bytes.drop(typeData.length + 1).takeWhile(_ != 0)
    val length = new String(lengthData.toList).toInt

    ObjectHeader(typ = t, length = length)
  }
}