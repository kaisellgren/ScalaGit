package git

import scala.collection.mutable.ListBuffer
import java.io.File
import git.util.FileUtil
import scala.Some

class Repository(val path: String, val wcPath: String) {
  var commits: CommitLog = _
  var database: ObjectDatabase = _
  var refs: ReferenceCollection = _
  var branches: List[BaseBranch] = _
  var packIndexes: List[PackIndex] = _
  var index: Index = _
  var tags: List[Tag] = _

  def head(): Option[BaseBranch] = refs.head match {
    case None => None
    case Some(head) => branches.find(_.tipId == head.targetIdentifier) match {
      case a: Some[BaseBranch] => Some(a.x)
      case _ => Some(DetachedHead(repository = this, tipId = head.targetIdentifier))
    }
  }
}

object Repository {
  def open(path: String): Repository = {
    // TODO: Find a better way.
    val workingCopyPath = path.replace(".git", "")
    val repositoryPath = workingCopyPath + "/.git"

    initializeRepository(repositoryPath)

    val repo = new Repository(repositoryPath, workingCopyPath)

    repo.refs = new ReferenceCollection(repo)
    repo.commits = new CommitLog(repo)
    repo.database = new ObjectDatabase(repo)
    repo.tags = findTags(repo)

    repo.refs.load()

    initializeIndex(repo)
    initializePackIndexes(repo)
    initializeBranches(repo)

    repo
  }

  private def isInitialized(path: String): Boolean = new File(path + "/HEAD").exists()

  private def initializeRepository(path: String) {
    // Always ensure we have the basic folder structure.
    new File(path).mkdirs()

    new File(s"$path/objects/pack").mkdirs()
    new File(s"$path/objects/info").mkdirs()
    new File(s"$path/refs/heads").mkdirs()
    new File(s"$path/refs/tags").mkdirs()
    new File(s"$path/refs/notes").mkdirs()
    new File(s"$path/refs/remotes").mkdirs()

    // If this repository does not exist (use wishes to create a new one), then set up the remaining files.
    if (!isInitialized(path)) {
      FileUtil.createFileWithContents(s"$path/description", "Unnamed repository; edit this file 'description' to name the repository.\n")
      FileUtil.createFileWithContents(s"$path/HEAD", "ref: refs/heads/master\n")

      // TODO: Let's implement a Config class.
      FileUtil.createFileWithContents(s"$path/config", "[core]\n\trepositoryformatversion = 0\n\tfilemode = false\n\tbare = false\n\tlogallrefupdates = true\n\tsymlinks = false\n\tignorecase = true\n\thideDotFiles = dotGitOnly")
    }
  }

  private def initializeIndex(repo: Repository) {
    repo.index = IndexFile.fromBytes(FileUtil.readContents(new File(s"${repo.path}/index")))
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
      val tagRef = ObjectId.fromBytes(FileUtil.readContents(file))

      repository.database.findObjectById(tagRef).get match {
        case obj: Tag => tagBuffer += obj
        case obj: Commit => tagBuffer += Tag.fromHashCode(ObjectId(FileUtil.readString(file).trim), repository = repository, name = file.getName)
      }
    })

    tagBuffer.toList
  }
}