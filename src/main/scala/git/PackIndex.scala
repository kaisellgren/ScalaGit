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

import scala.collection.mutable.ListBuffer
import git.util.{FileUtil, DataReader, Conversion}
import java.io.File

class PackIndex {
  var fanOutTable: Seq[Int] = _
  var objectIds: Seq[ObjectId] = _
  var offsets: Seq[Int] = _
  var packFile: PackFile = _
  var length = 0

  def has(id: ObjectId) = objectIds.contains(id)

  def getOffset(id: ObjectId): Option[Int] = {
    if (has(id)) Some(offsets(objectIds.indexOf(id)))
    else None
  }
}

object PackIndex {
  def fromPackIndexFile(bytes: Seq[Byte]): PackIndex = {
    val o = new PackIndex

    val reader = new DataReader(bytes)

    // Confirm the header is correct.
    if (reader.take(4).toArray.deep != Array(0xff, 0x74, 0x4f, 0x63).map(_.toByte).deep) throw new Exception("Index file header signature is corrupt.")

    // Confirm the version.
    if (reader.take(4).toArray.deep != Array(0, 0, 0, 2).map(_.toByte).deep) throw new Exception("Older Pack Index file format is not supported.")

    // Create the fan-out table.
    val fanOutBuffer = new ListBuffer[Int]

    for (i <- 0 to 255) fanOutBuffer += Conversion.bytesToValue(reader.take(4))

    o.fanOutTable = fanOutBuffer.toList

    // Set the length (the last value of the fan-out table).
    o.length = o.fanOutTable.last

    // Set the object id table.
    val objectIdBuffer = new ListBuffer[ObjectId]

    for (i <- 0 until o.length) objectIdBuffer += reader.takeObjectId()

    o.objectIds = objectIdBuffer.toList

    // Skip CRC32's for now.
    reader ++ o.length * 4

    // Let's set the offsets.
    val offsetBuffer = new ListBuffer[Int]

    // TODO: Implement support for very large offsets (>4 GB pack files).
    for (i <- 0 until o.length) offsetBuffer += Conversion.bytesToValue(reader.take(4))

    o.offsets = offsetBuffer.toList

    o
  }

  private[git] def findPackIndexes(repository: Repository): Seq[PackIndex] = {
    val buffer = Vector.newBuilder[PackIndex]

    new File(repository.path + "/objects/pack").listFiles.filter(_.getName.endsWith(".idx")).foreach((file: File) => {
      val index = PackIndex.fromPackIndexFile(FileUtil.readContents(file))
      val packName = file.getName.replace(".idx", ".pack")
      val pack = new PackFile
      pack.file = new File(repository.path + s"/objects/pack/$packName")
      pack.index = index
      index.packFile = pack
      buffer += index
    })

    buffer.result()
  }
}