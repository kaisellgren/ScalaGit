package git
package util

object Conversion {
  /**
   * Converts a sequence of bytes to an integer value.
   */
  def bytesToValue(bytes: Array[Short]): Int = {
    var value = 0

    for (i <- 0 to bytes.length - 1) {
      value += (bytes.reverse(i) * Math.pow(256, i)).toInt
    }

    value
  }
}
