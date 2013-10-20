package git

import collection.mutable.Stack
import org.scalatest._
import java.io.File

class RepositorySpec extends FlatSpec with Matchers {
  "A repository" should "open properly" in {
    val r = Repository.open(new File("src/test/resources/repositories/simple/.git").getAbsolutePath)
    r.tags.length should be (0)
  }

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