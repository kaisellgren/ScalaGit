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
import scala.annotation.tailrec

/** A simple data reader utility for data parsing and manipulation purposes.
  * This should not be used in threaded environment.
  */
class DataReader(data: Seq[Byte]) {
  private[this] var _position = 0

  /** Take `length` amount of bytes and move forward. */
  def take(length: Int): Seq[Byte] = {
    skip(length)
    data.slice(position - length, position)
  }

  /** Take data until the predicate is true. Moves forward. */
  def takeWhile(p: Byte => Boolean): Seq[Byte] = {
    val buffer = Vector.newBuilder[Byte]

    @tailrec
    def iterate(acc: Int): Unit = {
      val byte = data(position + acc)
      if (p(byte)) {
        buffer += byte
        iterate(acc + 1)
      }
    }

    iterate(0)

    val r = buffer.result()

    skip(r.length)

    r
  }

  /** Take data in form of a `String` until the predicate is true. Moves forward. */
  def takeStringWhile(p: Byte => Boolean): String = new String(takeWhile(p).toArray)

  /** Take a `String` based on the next `length` bytes and move forward. */
  def takeString(length: Int): String = new String(take(length).toArray)

  /** Returns an `ObjectId` based on a 40 byte `String`. Moves forward. */
  def takeStringBasedObjectId(): ObjectId = ObjectId(takeString(40))

  /** Returns an `ObjectId` based on raw data and moves forward. */
  def takeObjectId(): ObjectId = ObjectId.decode(take(ObjectId.RawSize))

  /** Returns the rest of the data. */
  def getRest: Seq[Byte] = data.takeRight(data.length - position)

  /** Returns the rest of the data as `String`. */
  def getRestAsString: String = new String(getRest.toArray)

  /** Take `length` amount of bytes without moving forward. */
  def get(length: Int): Seq[Byte] = data.slice(position, position + length)

  /** Skip `length` amount of bytes. */
  def skip(length: Int): Unit = {_position = _position + length}

  /** Returns the current internal position. */
  def position = _position

  /** Moves the internal position forward. */
  def >>(length: Int): Unit = skip(length)

  /** Moves the internal position backward. */
  def <<(length: Int): Unit = skip(-length)
}