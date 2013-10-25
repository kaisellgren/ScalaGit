package git
package util

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}

object FileUtil {
  def readString(file: File): String = new String(readContents(file).toArray)

  def readContents(file: File): List[Byte] = {
    val bis = new BufferedInputStream(new FileInputStream(file))
    val bytes = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toList

    bytes
  }

  def writeToFile(file: File, data: List[Byte]) = {
    if (!file.canWrite) throw new Exception(s"File is not writable: ${file.getName}")

    val fos = new FileOutputStream(file)
    try {
      fos.write(data.toArray)
    } finally {
      fos.close()
    }
  }
  
  def createFileWithContents(path: String, data: String) = {
    val f = new File(path)
    f.createNewFile()

    FileUtil.writeToFile(f, data.getBytes.toList)
  }
}
