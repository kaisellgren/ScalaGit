package git

import java.io.File
import java.util.{GregorianCalendar, TimeZone, Calendar, Date}
import git.util.Conversion

object test {
  def main(args: Array[String]) {
    // Create our default repository.
    val repo = Repository.open(new File(".").getAbsolutePath)

    repo.index.entries.foreach((e) => println(e.name))

    //println(repo.head().get.tip().message)
    repo.commits.find(new CommitFilter).foreach((c) => {
      //println(c.message)
    })
    repo.tags.foreach((t) => {
      //println(t.id)
    })
  }
}