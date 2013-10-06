package git

object test {
  def main(args: Array[String]) {
    val repo = Repository.open("C:/Projects/CRC32/.git")
    println(repo.head.tip.message)
    //println(repo.database.findObjectById(ObjectId.fromHash("4a2c8dfbc027cdfac771726c7366204ac5d13575")))
  }
}