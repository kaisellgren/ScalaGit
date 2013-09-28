package git

import java.io.{File}
import git.util.{Compressor, FileUtil}

class ObjectDatabase(repository: Repository) {

  /**
   * Finds one Git object from the object database by its identifier.
   */
  def findObjectById(id: ObjectId): Object = {
    // Try to look into the object files first.
    val file = new File(repository.path + s"/objects/${id.sha.take(2)}/${id.sha.substring(2)}")
    if (file.exists()) {
      // Let's create the object from the object file.

      // Read and decompress the data.
      val bytes = Compressor.decompressData(FileUtil.readContents(file))

      // Construct our header.
      val header = ObjectHeader.fromObjectFile(bytes)

      // The actual object contents without the header data.
      val objectFileData = bytes.takeRight(header.length)

      val obj = header.`type` match {
        case ObjectType.Commit => Commit.fromObjectFile(objectFileData)
        case ObjectType.Tree => Tree.fromObjectFile(objectFileData)
        case ObjectType.Blob => Blob.fromObjectFile(objectFileData)
        case _ => throw new NotImplementedError(s"Object type '${header.`type`}' is not implemented!")
      }

      obj.header = header
      obj.id = id
      obj.repository = repository

      obj
    } else {
      // No object file, let's look into the pack files.
      new Commit
    }
  }
}