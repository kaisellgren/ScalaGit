package git

import java.io.{File, FileInputStream}
import git.util.FileUtil

class ObjectDatabase(repository: Repository) {


  def findObjectById(id: ObjectId) {
    // Try to look into the object files first.
    val file = new File(repository.path + s"/objects/${id.sha.take(2)}/${id.sha.substring(2)}")
    if (file.exists()) {
      val contents = FileUtil.readContents(file)
      println(contents.length)
    } else {
      // No object file, let's look into the pack files.
    }
  }
}