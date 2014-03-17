package git

import org.scalatest._
import java.io.File
import java.util.{TimeZone, GregorianCalendar}

class CommitSpec extends FlatSpec with Matchers {
  /*"A commit" should "be parsed properly" in {
    val r = Repository.open(new File("src/test/resources/repositories/default/.git").getAbsolutePath)
    val c = ObjectDatabase.findObjectById(r, ObjectId("b744d5cddb5095249299d95ee531cbd990741140")).get.asInstanceOf[Commit]

    val cal = new GregorianCalendar(2000, 5, 4, 2, 3, 4)
    cal.setTimeZone(TimeZone.getTimeZone("UTC"))

    c.id should be (ObjectId("b744d5cddb5095249299d95ee531cbd990741140"))
    c.repository should be (r)
    c.header.typ should be (ObjectType.Commit)
    c.authorName should be ("Kai")
    c.authorEmail should be ("kaisellgren@gmail.com")
    c.authorDate should be (cal.getTime)
    c.committerName should be ("foo")
    c.committerEmail should be ("bar")
    c.commitDate should be (cal.getTime)
    c.message should be ("Whatsup")
    c.treeId should be (ObjectId("b744d5cddb5095249299d95ee531cbd990741140"))
    c.parentIds should be (Nil)
  }*/

  /*"A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be (2)
    stack.pop() should be (1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    }
  }*/
}