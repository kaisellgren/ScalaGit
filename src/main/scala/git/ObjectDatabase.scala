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

import java.io.File
import git.util.{Compressor, FileUtil}
import scala.annotation.tailrec
import java.security.MessageDigest

object ObjectDatabase {
  def hashObject(bytes: Seq[Byte]): ObjectId = {
    val digest = MessageDigest.getInstance("SHA-1")
    ObjectId.fromBytes(digest.digest(bytes.toArray))
  }

  /**
   * Finds one Git object from the object database by its identifier.
   */
  private[git] def findObjectById(id: ObjectId)(repository: Repository): Option[Object] = {
    // Try to look into the object files first.
    val file = new File(s"${repository.path}/objects/${id.sha.take(2)}/${id.sha.substring(2)}")

    // TODO: Swap the if.

    if (file.exists) {
      // Let's create the object from the object file.

      // Read and decompress the data.
      val bytes = Compressor.decompressData(FileUtil.readContents(file))

      // Construct our header.
      val header = ObjectHeader.decode(bytes)

      // The actual object contents without the header data.
      val objectFileData = bytes.takeRight(header.length)

      Some(header.typ match {
        case ObjectType.Commit => Commit.decode(objectFileData, id = Some(id))
        case ObjectType.Tree => Tree.decode(objectFileData, id = Some(id))
        case ObjectType.Blob => Blob.decode(objectFileData, id = Some(id))
        case ObjectType.Tag => Tag.decode(objectFileData, id = Some(id))
        case _ => throw new NotImplementedError(s"Object type '${header.typ}' is not implemented!")
      })
    } else {
      // No object file, let's look into the pack indices.
      @tailrec
      def find(indexes: Seq[PackIndex]): Option[Object] = {
        if (indexes.length == 0) None
        else indexes.head.findOffset(id) match {
          case Some(offset: Int) => Some(PackFile.findById(repository, indexes.head.packFile, offset, id))
          case _ => find(indexes.tail)
        }
      }

      find(PackIndex.findPackIndexes(repository))
    }
  }

  private[git] def deleteObjectById(id: ObjectId)(repository: Repository) = {
    val file = objectIdToObjectFile(repository, id)

    // Either it's an object file, or part of the pack file.
    if (file.exists()) file.delete()
    else {
      ???
    }
  }

  private [git] def deleteObject(o: Object)(repository: Repository) = deleteObjectById(o.id)(repository)

  private[git] def addObject(repository: Repository, obj: Object) = {
    val file = objectIdToObjectFile(repository, obj.id)
    file.mkdirs()

    FileUtil.writeToFile(file, Compressor.compressData(obj))
  }

  private[git] def objectIdToObjectFile(repository: Repository, id: ObjectId): File = {
    val folderName = id.sha.substring(0, 2)
    val filename = id.sha.substring(2)
    val folderPath = s"${repository.path}/objects/$folderName"

    new File(s"$folderPath/$filename")
  }

  /*def test(a: Int): State[Repository, Int] = State(repo => {
    (repo.copy(foos = repo.foos :+ a), a)
  })*/
}