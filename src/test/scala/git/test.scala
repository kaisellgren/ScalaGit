package git

import java.io.File
import java.util.{GregorianCalendar, TimeZone, Calendar, Date}
import git.util.Conversion

object test {
  def main(args: Array[String]) {
    // Create our default repository.
    val repo = Repository.open(new File("C:\\Projects\\CRC32").getAbsolutePath)

    /*println(repo.head().get.tip().message)
    repo.commits.find(new CommitFilter).foreach((c) => {
      println(c.message)
    })*/
  }
}