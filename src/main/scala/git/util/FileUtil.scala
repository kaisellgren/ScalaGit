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
}