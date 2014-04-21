package git

import org.scalatest._
import git.util.FileUtil
import java.io.File

class IgnoreSpec extends FlatSpec with Matchers {
  // Find files that we use to test ignore-rules against.
  val files = FileUtil.recursiveListFiles(new File("src/test/resources/ignore-tests"))

  val rules = List(
    "target/",
    "*.xml",
    "foo/bar/",
    "a/*.asd",
    "/*.test",
    "remove.txt",
    "",
    "#a.txt",
    "*.txta"
    //"!a.txt"
  )

  val shouldBeIgnored = List(
    "foo/target/a.txt",
    "foo/target",
    "exclude.xml",
    "a/exclude.xml",
    "a/b/exclude.xml",
    "foo/bar/a.txt",
    "foo/bar/",
    "a/asd.asd",
    "test.test",
    "a/remove.txt"
  ).map((s: String) => new File("src/test/resources/ignore-tests/" + s))

  val ignoreFile = Ignore(entries = rules, root = new File("src/test/resources/ignore-tests"))

  files.foreach((file) => {
    if (Ignore.isIgnored(ignoreFile, file)) {
      s"${file.getPath}" should "be ignored" in {
        assert(shouldBeIgnored.contains(file))
      }
    } else {
      s"${file.getPath}" should "NOT be ignored" in {
        assert(!shouldBeIgnored.contains(file))
      }
    }
  })
}