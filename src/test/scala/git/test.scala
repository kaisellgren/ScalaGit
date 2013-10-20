package git

import java.io.File

object test {
  def main(args: Array[String]) {
    val repo = Repository.open(new File("src/test/resources/repositories/test").getAbsolutePath)
    //println(repo.head().tip().message)
    println(repo.tags.length)
    /*repo.commits.find(CommitFilter()).foreach(c => {
      println(c.id)
      println(c.message)
    })*/

    //println(repo.tags.length)
    //repo.tags.foreach(println)
  }
}