package git
package util

import java.io.{BufferedInputStream, File, FileInputStream}

object FileUtil {
  def readContents(file: File): Array[Short] = {
    val bis = new BufferedInputStream(new FileInputStream(file))
    val bytes = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toShort).toArray

    bytes
  }
}
