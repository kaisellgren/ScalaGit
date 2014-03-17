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

import java.io.{RandomAccessFile, File}
import git.util.{Compressor, FileUtil}
import scalaz._
import Scalaz._
import scala.io.Source
import scalaz.Source
import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec

object ObjectDatabase {
  def hashObject(bytes: List[Byte]) = ???

  /**
   * Finds one Git object from the object database by its identifier.
   */
  private[git] def findObjectById(repository: Repository, id: ObjectId): Option[Object] = {
    // Try to look into the object files first.
    val file = new File(s"${repository.path}/objects/${id.sha.take(2)}/${id.sha.substring(2)}")

    if (file.exists) {
      // Let's create the object from the object file.

      // Read and decompress the data.
      val bytes = Compressor.decompressData(FileUtil.readContents(file))

      // Construct our header.
      val header = ObjectHeader.fromObjectFile(bytes)

      // The actual object contents without the header data.
      val objectFileData = bytes.takeRight(header.length)

      Some(header.typ match {
        case ObjectType.Commit => Commit.fromObjectFile(objectFileData, id = id, repository = repository, header = Some(header))
        case ObjectType.Tree => Tree.fromObjectFile(objectFileData, id = id, repository = repository, header = Some(header))
        case ObjectType.Blob => Blob.fromObjectFile(objectFileData, id = id, repository = repository, header = Some(header))
        case ObjectType.Tag => Tag.fromObjectFile(objectFileData, id = id, repository = repository, header = Some(header))
        case _ => throw new NotImplementedError(s"Object type '${header.typ}' is not implemented!")
      })
    } else {
      // No object file, let's look into the pack indices.
      @tailrec
      def find(indexes: Seq[PackIndex]): Option[Object] = {
        if (indexes.length == 0) None
        else indexes.head.getOffset(id) match {
          case Some(offset: Int) => Some(indexes.head.packFile.loadObject(offset, id, repository))
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