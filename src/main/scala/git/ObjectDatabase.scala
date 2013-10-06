package git

import java.io.{RandomAccessFile, File}
import git.util.{Compressor, FileUtil}

class ObjectDatabase(repository: Repository) {

  /**
   * Finds one Git object from the object database by its identifier.
   */
  def findObjectById(id: ObjectId): Option[Object] = {
    // Try to look into the object files first.
    val file = new File(repository.path + s"/objects/${id.sha.take(2)}/${id.sha.substring(2)}")

    if (file.exists) {
      // Let's create the object from the object file.

      // Read and decompress the data.
      val bytes = Compressor.decompressData(FileUtil.readContents(file))

      // Construct our header.
      val header = ObjectHeader.fromObjectFile(bytes)

      // The actual object contents without the header data.
      val objectFileData = bytes.takeRight(header.length)

      val o = header.`type` match {
        case ObjectType.Commit => Commit.fromObjectFile(objectFileData)
        case ObjectType.Tree => Tree.fromObjectFile(objectFileData)
        case ObjectType.Blob => Blob.fromObjectFile(objectFileData)
        case ObjectType.Tag => Tag.fromObjectFile(objectFileData)
        case _ => throw new NotImplementedError(s"Object type '${header.`type`}' is not implemented!")
      }

      o.header = header
      o.id = id
      o.repository = repository

      Some(o)
    } else {
      println("Debug: Finding from pack files.")

      // No object file, let's look into the pack indices.
      val o = repository.packIndexes.collectFirst {
        case i: PackIndex => {
          i.getOffset(id) match {
            case Some(offset: Int) => i.packFile.loadObject(offset, id)
            case _ => None
          }
        }
      }

      Some(o.get.asInstanceOf[Object]) // TODO: Any way to avoid this mess?
    }
  }
}