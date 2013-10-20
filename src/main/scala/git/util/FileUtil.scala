package git
package util

import java.io.{PrintWriter, BufferedInputStream, File, FileInputStream}

object FileUtil {
  def readContents(file: File): Array[Short] = {
    val bis = new BufferedInputStream(new FileInputStream(file))
    val bytes = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toShort).toArray

    bytes
  }

  def writeToFile(file: File, data: String) = {
    val writer = new PrintWriter(file)
    writer.write(data)
    writer.close()
  }
  
  def createFileWithContents(path: String, data: String) = {
    val f = new File(path)
    f.createNewFile()

    FileUtil.writeToFile(f, data)
  }
}
