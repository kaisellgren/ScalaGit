package git

import org.scalatest._
import git.util.Glob

class GlobSpec extends FlatSpec with Matchers {
  "Glob matching" should "work" in {
    val successful = List(
      ("*.xml", "foo/bar/baz.xml"),
      ("foo/bar/", "foo/bar/baz"),
      ("test/", "foo/test/bar"),
      ("test/*.asd", "test/baz.asd")
      //("/*.xml", "foo.xml")
    )

    val failure = List(
      ("", "something"),
      ("#", "something"),
      ("*.xmla", "foo/bar/baz.xml"),
      ("!bar", "bar"),
      ("foo/bar/", "foo/bar"), // Shouldn't match file "bar".
      ("foo/bar/baz", "foo/bar/heh"),
      ("test/*.asd", "test/foo.basd"),
      ("test/*.asd", "test/bar/huh.asd"),
      ("/*.xml", "bar/foo.xml")
    )

    successful.foreach((parts) => {
      assert(Glob.matches(parts._1, parts._2), s"'${parts._1}' should match '${parts._2}'")
    })

    failure.foreach((parts) => {
      assert(!Glob.matches(parts._1, parts._2), s"'${parts._1}' should NOT match '${parts._2}'")
    })
  }
}