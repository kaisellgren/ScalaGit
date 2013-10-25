package git
package util

import java.math.BigInteger

object Conversion {
  /**
   * Converts a sequence of bytes to an integer value.
   */
  def bytesToValue(bytes: List[Byte]): Int = new BigInteger(1, bytes).intValue
}