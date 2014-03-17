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

package git.util

import git.ObjectId
import scala.collection.mutable.ListBuffer

/** A simple data reader utility that abstracts out the mutable state. */
class DataReader(data: Seq[Byte]) {
  private var _position = 0

  def position = _position

  /** Take `length` amount of bytes and move forward. */
  def take(length: Int): Seq[Byte] = {
    _position += length
    data.slice(position - length, position)
  }

  /** Take data until the predicate is true. Moves forward. */
  def takeWhile(p: Byte => Boolean): Seq[Byte] = {
    val buffer = new ListBuffer[Byte]

    var i = 0
    while (p(data.slice(position + i, position + i + 1)(0))) {
      buffer += data.slice(position + i, position + i + 1)(0)
      i += 1
    }

    _position += i

    buffer.toList
  }

  /** Take data in form of a `String` until the predicate is true. Moves forward. */
  def takeStringWhile(p: Byte => Boolean): String = new String(takeWhile(p).toArray)

  /** Take a `String` based on the next `length` bytes and move forward. */
  def takeString(length: Int): String = new String(take(length).toArray)

  /** Returns an `ObjectId` based on a 40 byte `String`. Moves forward. */
  def takeStringBasedObjectId(): ObjectId = ObjectId(takeString(40))

  /** Returns an `ObjectId` based on raw data and moves forward. */
  def takeObjectId(): ObjectId = ObjectId.fromBytes(take(ObjectId.RawSize))

  /** Returns the rest of the data. */
  def getRest: Seq[Byte] = data.takeRight(data.length - position)

  /** Returns the rest of the data as `String`. */
  def getRestAsString: String = new String(getRest.toArray)

  /** Take `length` amount of bytes without moving forward. */
  def get(length: Int): Seq[Byte] = data.slice(position, position + length)

  /** Skip `length` amount of bytes. */
  def skip(length: Int) = _position += length

  def ++(length: Int) = skip(length)
  def --(length: Int) = skip(-length)
}