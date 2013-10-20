package git

import java.io.File
import scala.io._
import scala.collection.mutable.ListBuffer

class ReferenceCollection(val repository: Repository) {
  var head: Option[Reference] = _
  var localReferences: List[Reference] = _
  var remoteReferences: List[Reference] = _

  def load(): Unit = {
    // Create references based on the refs folder.
    localReferences = new File(repository.path + Reference.LocalBranchPrefix).listFiles.map(file => Reference(file.getName, ObjectId(Source.fromFile(file).mkString.trim))).toList

    // TODO: More functional style?
    /*remoteReferences = new File(repository.path + Reference.RemoteTrackingBranchPrefix).listFiles.foldLeft(new ListBuffer) { (previous, file: File) => {
        case file.isDirectory => {
          previous ++ file.listFiles
        }
        case _ => Nil
      }
    }*/
    val buffer = new ListBuffer[Reference]
    new File(repository.path + Reference.RemoteTrackingBranchPrefix).listFiles.foreach((folder: File) => {
      if (folder.isDirectory) {
        folder.listFiles.foreach((file: File) => {
          val ref = Reference(file.getName, ObjectId(Source.fromFile(file).mkString.trim))
          ref.remote = folder.getName
          if (file.getName != "HEAD") buffer += ref
        })
      }
    })
    remoteReferences = buffer.toList

    // Create the HEAD reference. Read the HEAD file and figure out the canonical name.
    val regex = s"""(?s).*refs/heads/([a-zA-Z0-9]+).*""".r
    val canonicalName = regex.findFirstMatchIn(Source.fromFile(repository.path + "/HEAD").mkString).get.group(1)
    head = localReferences.find((r: Reference) => r.canonicalName == canonicalName)
  }
}