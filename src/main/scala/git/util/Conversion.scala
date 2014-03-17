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
package util

import java.math.BigInteger

object Conversion {
  val hexArray = "0123456789abcdef".toCharArray

  /** Converts a sequence of bytes to an integer value. */
  def bytesToValue(bytes: Seq[Byte]): Int = new BigInteger(1, bytes.toList).intValue

  /** Converts bytes into a hex string. */
  def bytesToHexString(bytes: Seq[Byte]): String = {
    // This method is optimized for performance.
    val hexChars = new Array[Char](bytes.length * 2)

    for (i: Int <- 0 until bytes.length) {
      val value = bytes(i) & 0xff
      hexChars(i * 2) = hexArray(value >>> 4)
      hexChars(i * 2 + 1) = hexArray(value & 0x0f)
    }

    new String(hexChars)
  }
}