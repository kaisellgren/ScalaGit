package git.util

import java.io.{File, FileInputStream}

object FileUtil {
  def readContents(file: File): Array[Byte] = {
    // TODO: Let's have a buffer here...
    val in = new FileInputStream(file)
    val bytes = new Array[Byte](file.length.toInt)

    try {
      in.read(bytes)
    } finally {
      in.close()
    }

    bytes
  }
}
