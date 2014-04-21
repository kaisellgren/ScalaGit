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

import java.nio.ByteBuffer
import javax.xml.bind.DatatypeConverter

object Conversion {
  val hexArray = "0123456789abcdef".toCharArray

  /** Converts a sequence of bytes to an integer value. */
  def bytesToInt(bytes: Seq[Byte]): Int = {
    bytes.length match {
      case 0 => 0
      case 1 => bytes(0)
      case 2 => ByteBuffer.wrap(bytes.toArray[Byte]).getShort
      case 4 => ByteBuffer.wrap(bytes.toArray[Byte]).getInt
      case _ => throw new Exception(s"Cannot convert ${bytes.length} amount of bytes into an integer value.")
    }
  }

  /** Converts an integer to a sequence of bytes. */
  def intToBytes(value: Int): Seq[Byte] = {
    val buffer = new Array[Byte](4)

    for (i <- 0 until 4) {
      val offset = (buffer.length - 1 - i) * 8
      buffer(i) = ((value >>> offset) & 0xff).toByte
    }

    buffer
  }

  /** Converts a short to a sequence of bytes. */
  def shortToBytes(value: Short): Seq[Byte] = {
    val buffer = new Array[Byte](2)

    for (i <- 0 until 2) {
      val offset = (buffer.length - 1 - i) * 8
      buffer(i) = ((value >>> offset) & 0xff).toByte
    }

    buffer
  }

  /** Converts bytes into a hex string. */
  def bytesToHexString(bytes: Seq[Byte]): String = {
    val hexChars = new Array[Char](bytes.length * 2)

    for (i <- 0 until bytes.length) {
      val value = bytes(i) & 0xff
      hexChars(i * 2) = hexArray(value >>> 4)
      hexChars(i * 2 + 1) = hexArray(value & 0x0f)
    }

    new String(hexChars)
  }

  /** Converts a hex string into a byte sequence. */
  def hexStringToBytes(hex: String): Seq[Byte] = DatatypeConverter.parseHexBinary(hex)
}