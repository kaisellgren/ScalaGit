package git

import scala.collection.mutable.ListBuffer
import java.io.File
import git.util.FileUtil

class Repository(var path: String) {
  var commits: CommitLog = _
  var database: ObjectDatabase = _
  var refs: ReferenceCollection = _
  var branches: List[Branch] = _
  var packIndexes: List[PackIndex] = _

  def head: Branch = {
    branches.find((b) => b.tipId == refs.head.targetIdentifier) match {
      case a: Some[Branch] => a.x
      case _ => {
        val b = new DetachedHead()
        b.repository = this
        b.name = "(no branch)"
        b.canonicalName = b.name
        b.tipId = refs.head.targetIdentifier
        b
      }
    }
  }
}

object Repository {
  def open(path: String): Repository = {
    val repo = new Repository(path)

    repo.refs = new ReferenceCollection(repo)
    repo.commits = new CommitLog(repo)
    repo.database = new ObjectDatabase(repo)

    repo.refs.load()

    initializeBranches(repo)
    initializePackIndexes(repo)

    repo
  }

  private def initializePackIndexes(repo: Repository) {
    val buffer = new ListBuffer[PackIndex]

    new File(repo.path + "/objects/pack").listFiles.filter(_.getName.endsWith(".idx")).foreach((file: File) => {
      val index = PackIndex.fromPackIndexFile(FileUtil.readContents(file))
      val packName = file.getName.replace(".idx", ".pack")
      index.packFile = new File(repo.path + s"/objects/pack/$packName")
      buffer += index
    })

    repo.packIndexes = buffer.toList
  }

  private def initializeBranches(repo: Repository) = {
    val buffer = new ListBuffer[Branch]

    // Construct branches.
    (repo.refs.localReferences ++ repo.refs.remoteReferences).foreach((r) => {
      val b = new Branch
      b.tipId = r.targetIdentifier
      b.repository = repo
      b.name = r.canonicalName
      b.isRemote = repo.refs.remoteReferences.contains(r)
      b.canonicalName = if (b.isRemote) s"remotes/${r.remote}/${b.name}" else s"origin/${b.name}"

      buffer += b
    })

    repo.branches = buffer.toList
  }
}