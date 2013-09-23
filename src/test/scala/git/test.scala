package git

object test {
  def main(args: Array[String]) {
    val repo = Repository.open("C:/Projects/ScalaGit/.git")
    repo.commits.find(new CommitFilter())//.forEach(println)
  }
}