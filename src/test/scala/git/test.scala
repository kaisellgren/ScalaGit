package git

object test {
  def main(args: Array[String]) {
    val repo = Repository.open("C:/Projects/Animation/.git")
    testTags(repo)
  }

  def testTags(repo: Repository) = {
    repo.tags.foreach(println)
  }
}