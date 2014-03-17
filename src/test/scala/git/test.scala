package git

import java.io.File

object test {
  def time[A](name: String)(p: => A): A = {
    val t = System.currentTimeMillis()
    val result = p
    val took = System.currentTimeMillis() - t
    println(s"$name: ${took}ms")
    result
  }

  def main(args: Array[String]) {
    val repo = Repository.open(new File(".").getAbsolutePath)


    //val tag = git.tag("testing")(repo)
    //println(tag)
    //Tag.delete("testing")(repo)
    Tag.find(repo).foreach(println)
    //}
    /*println(repo.foos)

    val (repo1, tags) = ObjectDatabase.test(5)(repo)
    println(tags)
    println(repo1.foos)*/

    //repo.createTag("Foo")
    //repo.status().entries.foreach((e) => {})
    /*println("asd")
    atomic { implicit txn =>
      repo.tags.get.foreach(println)
    }
    println("asd2")*/
    //println(repo.head().get.tip().message)
    //repo.commits.find(new CommitFilter).foreach((c) => {
      //println(c.message)
    //})
    //repo.tags.foreach((t) => {
      //println(t.id)
    //})
  }
}
