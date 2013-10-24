package git
package util

import java.io.{PrintWriter, BufferedInputStream, File, FileInputStream, FileOutputStream}

object FileUtil {
  def readContents(file: File): Array[Short] = {
    val bis = new BufferedInputStream(new FileInputStream(file))
    val bytes = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toShort).toArray

    bytes
  }

  def writeToFile(file: File, data: Array[Byte]) = {
    if (!file.canWrite) throw new Exception(s"File is not writable: ${file.getName}")

    val fos = new FileOutputStream(file)
    try {
      fos.write(data)
    } finally {
      fos.close()
    }
  }
  
  def createFileWithContents(path: String, data: String) = {
    val f = new File(path)
    f.createNewFile()

    FileUtil.writeToFile(f, data.getBytes)
  }
}
