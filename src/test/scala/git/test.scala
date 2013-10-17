package git

object test {
  def main(args: Array[String]) {
    val repo = Repository.open("C:/Projects/CRC32/.git")
    println(repo.head().tip().message)
    repo.commits.find(new CommitFilter).foreach(c => {
      println(c.id)
      println(c.message)
    })
  }
}