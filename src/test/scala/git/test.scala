package git

import java.io.File
import java.util.Date

object test {
  def main(args: Array[String]) {
    val repo = Repository.open(new File("src/test/resources/repositories/test").getAbsolutePath)
    val c = Commit(
      id = ObjectId("b744d5cddb5095249299d95ee531cbd990741140"),
      repository = repo,
      header = ObjectHeader(ObjectType.Commit),
      authorName = "Kai",
      authorEmail = "kaisellgren@gmail.com",
      authorDate = new Date(),
      committerName = "foo",
      committerEmail = "bar",
      commitDate = new Date(),
      message = "Whatsup",
      treeId = ObjectId("b744d5cddb5095249299d95ee531cbd990741140"),
      parentIds = Nil
    )

    val bytes = Commit.toObjectFile(c)
    val c2 = Commit.fromObjectFile(bytes.toArray.map(_.toShort), repository = repo, id = ObjectId("b744d5cddb5095249299d95ee531cbd990741140"), header = None)
    println(c2)

    //println(repo.head().tip().message)
    //println(repo.tags.length)
    /*repo.commits.find(CommitFilter()).foreach(c => {
      println(c.id)
      println(c.message)
    })*/

    //println(repo.tags.length)
    //repo.tags.foreach(println)
  }
}