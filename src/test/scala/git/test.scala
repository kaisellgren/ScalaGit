package git

object test {
  def main(args: Array[String]) {
    val repo = Repository.open("C:/Projects/Animation/.git")
    println(repo.head)
  }
}