package git

import scala.collection.mutable.ListBuffer
import java.io.{PrintWriter, File}
import git.util.FileUtil
import git.util.FileUtil._
import scala.Some

class Repository(val path: String, val wcPath: String) {
  var commits: CommitLog = _
  var database: ObjectDatabase = _
  var refs: ReferenceCollection = _
  var branches: List[BaseBranch] = _
  var packIndexes: List[PackIndex] = _
  var tags: List[Tag] = _

  def head(): BaseBranch = {
    branches.find((b) => b.tipId == refs.head.targetIdentifier) match {
      case a: Some[BaseBranch] => a.x
      case _ => DetachedHead(repository = this, tipId = refs.head.targetIdentifier)
    }
  }

  private def isInitialized(): Boolean = new File(path + "/HEAD").exists()
}

object Repository {
  def open(path: String): Repository = {
    // TODO: Find a better way.
    val workingCopyPath = path.replace(".git", "")
    val repositoryPath = workingCopyPath + "/.git"

    val repo = new Repository(repositoryPath, workingCopyPath)


    repo.refs = new ReferenceCollection(repo)
    repo.commits = new CommitLog(repo)
    repo.database = new ObjectDatabase(repo)
    repo.tags = findTags(repo)

    repo.refs.load()

    initializePackIndexes(repo)
    initializeBranches(repo)

    repo
  }

  private def initializeRepository(path: String) {
    new File(path + "/config").createNewFile()
    val head = new File(path + "/HEAD")
    head.createNewFile()
    val writer = new PrintWriter(head)
    writer.write("ref: refs/heads/master\n")
    writer.close()
    //head.
    //Path("").to
    //Resource.fromFile("")
    //Source.fromFile(head).
    //new
  }

  private def initializePackIndexes(repo: Repository) {
    val buffer = new ListBuffer[PackIndex]

    new File(repo.path + "/objects/pack").listFiles.filter(_.getName.endsWith(".idx")).foreach((file: File) => {
      val index = PackIndex.fromPackIndexFile(FileUtil.readContents(file))
      val packName = file.getName.replace(".idx", ".pack")
      val pack = new PackFile
      pack.file = new File(repo.path + s"/objects/pack/$packName")
      pack.index = index
      index.packFile = pack
      buffer += index
    })

    repo.packIndexes = buffer.toList
  }

  private def initializeBranches(repo: Repository) = {
    val buffer = new ListBuffer[BaseBranch]

    // Construct branches.
    (repo.refs.localReferences ++ repo.refs.remoteReferences).foreach((r) => {
      val b = if (repo.refs.remoteReferences.contains(r)) RemoteBranch(
          repository = repo,
          tipId = r.targetIdentifier,
          name = r.canonicalName,
          canonicalName = s"remotes/${r.remote}/${r.canonicalName}",
          trackedBranch = None
        ) else Branch(
          repository = repo,
          tipId = r.targetIdentifier,
          name = r.canonicalName,
          canonicalName = s"origin/${r.canonicalName}",
          trackedBranch = None // TODO: Implement.
        )

      buffer += b
    })

    repo.branches = buffer.toList
  }

  private def findTags(repository: Repository): List[Tag] = {
    val tagBuffer = new ListBuffer[Tag]

    new File(repository.path + Reference.TagPrefix).listFiles().foreach((file: File) => {
      // We read the value inside the tag file to see if it points to a tag, if so, we can add more info about it.
      val tagRef = ObjectId.fromBytes(readContents(file))

      repository.database.findObjectById(tagRef).get match {
        case obj: Tag => tagBuffer += obj
        case obj: Commit => tagBuffer += Tag.fromHashCode(ObjectId(file.getName), repository = repository)
      }
    })

    tagBuffer.toList
  }
}