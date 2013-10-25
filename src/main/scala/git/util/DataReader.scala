package git.util

import git.ObjectId
import scala.collection.mutable.ListBuffer

/** A simple data reader utility that abstracts out the mutable state. */
class DataReader(data: List[Byte]) {
  private var _position = 0

  def position = _position

  /** Take `length` amount of bytes and move forward. */
  def take(length: Int): List[Byte] = {
    _position += length
    data.slice(position - length, position)
  }

  /** Take data until the predicate is true. Moves forward. */
  def takeWhile(p: Byte => Boolean): List[Byte] = {
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
  def getRest: List[Byte] = data.takeRight(data.length - position)

  /** Returns the rest of the data as `String`. */
  def getRestAsString: String = new String(getRest.toArray)

  /** Take `length` amount of bytes without moving forward. */
  def get(length: Int): List[Byte] = data.slice(position, position + length)

  /** Skip `length` amount of bytes. */
  def skip(length: Int) = _position += length

  def ++(length: Int) = skip(length)
  def --(length: Int) = skip(-length)
}