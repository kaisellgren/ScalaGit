package git

object test {
  def main(args: Array[String]) {
    val repo = Repository.open("C:/Projects/CRC32/.git")
    //println(repo.head().tip().message)
    repo.commits.find(CommitFilter()).foreach(c => {
      println(c.id)
      println(c.message)
    })

    //println(repo.tags.length)
    //repo.tags.foreach(println)
  }
}