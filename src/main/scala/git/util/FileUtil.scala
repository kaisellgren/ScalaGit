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

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.nio.file.{FileSystems, Files}
import java.nio.file.attribute.FileTime

case class FileStat(
  ctime: Int,
  ctimeFractions: Int,
  mtime: Int,
  mtimeFractions: Int,
  device: Int,
  inode: Int,
  mode: Int,
  uid: Int,
  gid: Int,
  size: Int
)

object FileUtil {
  def readString(file: File): String = new String(readContents(file).toArray)

  def readContents(file: File): Seq[Byte] = {
    val bis = new BufferedInputStream(new FileInputStream(file))
    val bytes = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toList
    bis.close()

    bytes
  }

  def writeToFile(file: File, data: Seq[Byte]) = {
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

  def recursiveListFiles(file: File, ignoreDirectories: Seq[File] = Seq()): Array[File] = {
    val these = file.listFiles.filterNot((f) => ignoreDirectories.contains(f))

    these ++ these.filter(_.isDirectory).flatMap((f: File) => recursiveListFiles(f, ignoreDirectories = ignoreDirectories))
  }

  /** Implementation of Unix stat(2). */
  def stat(file: File): FileStat = {
    val isUnix = Files.getFileStore(file.toPath).supportsFileAttributeView("unix")

    val (ctimeSeconds, ctimeFractions) = {
      if (isUnix) {
        val ctime = Files.getAttribute(file.toPath, "unix:ctime").asInstanceOf[FileTime].toMillis
        val ctimeSeconds = math.floor(ctime / 1000).toInt
        val ctimeFractions = (ctime - ctimeSeconds).toInt

        (ctimeSeconds, ctimeFractions)
      } else {
        val ctime = Files.getAttribute(file.toPath, "basic:creationTime").asInstanceOf[FileTime].toMillis
        val ctimeSeconds = math.floor(ctime / 1000).toInt
        val ctimeFractions = (ctime - ctimeSeconds).toInt
        (ctimeSeconds, 0/*ctimeFractions*/)
      }
    }

    val mtime = Files.getAttribute(file.toPath, "basic:lastModifiedTime").asInstanceOf[FileTime].toMillis
    val mtimeSeconds = math.floor(mtime / 1000).toInt
    val mtimeFractions = 0//(mtime - mtimeSeconds).toInt

    val device = if (isUnix) Files.getAttribute(file.toPath, "unix:dev").asInstanceOf[Int] else 0
    val inode = if (isUnix) Files.getAttribute(file.toPath, "unix:ino").asInstanceOf[Int] else 0
    val mode = if (isUnix) Files.getAttribute(file.toPath, "unix:mode").asInstanceOf[Int] else 0 // 100644, 1000 0001 1010 0100 for regular file and readable
    val uid = if (isUnix) Files.getAttribute(file.toPath, "unix:uid").asInstanceOf[Int] else 0
    val gid = if (isUnix) Files.getAttribute(file.toPath, "unix:gid").asInstanceOf[Int] else 0

    val size = Files.getAttribute(file.toPath, "basic:size").asInstanceOf[Long].toInt

    FileStat(ctimeSeconds, ctimeFractions, mtimeSeconds, mtimeFractions, device, inode, mode, uid, gid, size)
  }
}