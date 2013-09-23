package git

import java.io.File
import scala.io._

class ReferenceCollection(val repository: Repository) {
  var head: Reference = _
  var references: List[Reference] = _

  def load(): Unit = {
    // Create references based on the refs folder.
    references = new File(repository.path + "/refs/heads").listFiles.map(file => Reference(file.getName, ObjectId(Source.fromFile(file).mkString))).toList

    // Create the HEAD reference. Read the HEAD file and figure out the canonical name.
    val regex = s"""(?s).*${Reference.LocalBranchPrefix}([a-zA-Z0-9]+).*""".r
    val canonicalName = regex.findFirstMatchIn(Source.fromFile(repository.path + "/HEAD").mkString).get.group(1)
    head = references.find((r: Reference) => r.canonicalName == canonicalName).get
  }
}