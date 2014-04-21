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
  /** Returns the ObjectId for the given sequence of bytes. */
  def hashObject(bytes: Seq[Byte]): ObjectId = {
    val digest = MessageDigest.getInstance("SHA-1")
    ObjectId.decode(digest.digest(bytes.toArray))
  }

  /** Finds the object from the database for the given ID. */
  private[git] def findObjectById(id: ObjectId)(repository: Repository): Option[Object] = {
    // Try to look into the object files first.
    val file = new File(s"${repository.path}/objects/${id.sha.substring(0, 2)}/${id.sha.substring(2)}")

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
        case ObjectType.Commit => Commit.decodeBody(objectFileData, id = Some(id), header = Some(header))
        case ObjectType.Tree => Tree.decodeBody(objectFileData, id = Some(id), header = Some(header))
        case ObjectType.Blob => Blob.decodeBody(objectFileData, id = Some(id), header = Some(header))
        case ObjectType.Tag => Tag.decodeBody(objectFileData, id = Some(id), header = Some(header))
        case _ => throw new NotImplementedError(s"Object type '${header.typ}' is not implemented!")
      })
    } else {
      // No object file, let's look into the pack indices.
      @tailrec
      def find(indexes: Seq[PackIndex]): Option[Object] = {
        if (indexes.length == 0) None
        else PackIndex.findOffset(indexes.head, id) match {
          case Some(offset: Int) => Some(PackFile.findById(repository, indexes.head.packFile, offset, id))
          case _ => find(indexes.tail.toSeq)
        }
      }

      find(PackIndex.findPackIndexes(repository))
    }
  }

  /** Deletes the given object from the database by its ID. */
  private[git] def deleteObjectById(id: ObjectId)(repository: Repository): Unit = {
    val file = objectIdToObjectFile(repository, id)

    // Either it's an object file, or part of the pack file.
    if (file.exists()) file.delete()
    else {
      ???
    }
  }

  /** Deletes the given object from the database. */
  private [git] def deleteObject(o: Object)(repository: Repository): Unit = deleteObjectById(o.id)(repository)

  /** Adds the given object to the database. */
  private[git] def addObject(repository: Repository, obj: Object): Unit = {
    val file = objectIdToObjectFile(repository, obj.id)
    file.mkdirs()

    FileUtil.writeToFile(file, Compressor.compressData(obj))
  }

  /** Returns the object File representing the given ObjectId. */
  private[git] def objectIdToObjectFile(repository: Repository, id: ObjectId): File = {
    val folderName = id.sha.substring(0, 2)
    val filename = id.sha.substring(2)
    val folderPath = s"${repository.path}/objects/$folderName"

    new File(s"$folderPath/$filename")
  }
}