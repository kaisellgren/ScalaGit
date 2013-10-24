package git

import java.io.File
import java.util.{GregorianCalendar, TimeZone, Calendar, Date}

object CreateRepositories {
  def main(args: Array[String]) {
    // Create our default repository.
    val repo = Repository.open(new File("src/test/resources/repositories/default").getAbsolutePath)

    val cal = new GregorianCalendar(2000, 5, 4, 2, 3, 4)
    cal.setTimeZone(TimeZone.getTimeZone("UTC"))

    val c = Commit(
      id = ObjectId("b744d5cddb5095249299d95ee531cbd990741140"),
      repository = repo,
      header = ObjectHeader(ObjectType.Commit),
      authorName = "Kai",
      authorEmail = "kaisellgren@gmail.com",
      authorDate = cal.getTime,
      committerName = "foo",
      committerEmail = "bar",
      commitDate = cal.getTime,
      message = "Whatsup",
      treeId = ObjectId("b744d5cddb5095249299d95ee531cbd990741140"),
      parentIds = Nil
    )

    repo.database += c
  }
}