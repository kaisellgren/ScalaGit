package git

import org.scalatest.{Matchers, FlatSpec}
import java.io.File
import git.util.FileUtil
import org.joda.time.{DateTimeZone, DateTime}

class CommitSpec extends FlatSpec with Matchers {
  // Create an example commit.
  val commit = Commit(
    id = ObjectId("6987b5626be09f59e84cb64c36b8e8a15a198798"),
    header = ObjectHeader(ObjectType.Commit),
    authorName = "Kai",
    authorDate = new DateTime(2014, 1, 2, 3, 4, 5, DateTimeZone.forOffsetHours(2)).toDate,
    authorEmail = "kaisellgren@gmail.com",
    commitDate = new DateTime(2014, 1, 2, 3, 4, 6, DateTimeZone.forOffsetHours(2)).toDate,
    committerEmail = "kaisellgren+foo@gmail.com",
    committerName = "Kai 2",
    message = "foo bar baz qux",
    treeId = ObjectId("b744d5cddb5095249299d95ee531cbd990741140"),
    parentIds = Seq(ObjectId("b744d5cddb5095249299d95ee531cbd990741141"), ObjectId("b744d5cddb5095249299d95ee531cbd990741142"))
  )

  "an encoded commit" should "decode back to itself" in {
    val commit2 = Commit.decode(Commit.encode(commit))

    commit2.id.sha shouldBe commit.id.sha
    commit2.header.typ shouldBe commit.header.typ
    commit2.authorName shouldBe commit.authorName
    commit2.authorDate shouldBe commit.authorDate
    commit2.authorEmail shouldBe commit.authorEmail
    commit2.commitDate shouldBe commit.commitDate
    commit2.committerEmail shouldBe commit.committerEmail
    commit2.committerName shouldBe commit.committerName
    commit2.message shouldBe commit.message
    commit2.treeId shouldBe commit.treeId
    commit2.parentIds shouldBe commit.parentIds
  }

  // Testing fixtures.
  val commit2 = Commit.decode(FileUtil.readContents(new File("src/test/resources/objects/commit/6987b5626be09f59e84cb64c36b8e8a15a198798")))

  "commit 6987b5626be09f59e84cb64c36b8e8a15a198798" should "have an ID of '6987b5626be09f59e84cb64c36b8e8a15a198798'" in {
    commit2.id.sha shouldBe "6987b5626be09f59e84cb64c36b8e8a15a198798"
  }

  it should "be type of Commit" in {
    commit2.header.typ shouldBe ObjectType.Commit
  }

  it should "have correct author details" in {
    commit2.authorDate shouldBe new DateTime(2014, 1, 2, 3, 4, 5, DateTimeZone.forOffsetHours(2)).toDate
    commit2.authorEmail shouldBe "kaisellgren@gmail.com"
    commit2.authorName shouldBe "Kai"
  }

  it should "have correct committer details" in {
    commit2.commitDate shouldBe new DateTime(2014, 1, 2, 3, 4, 6, DateTimeZone.forOffsetHours(2)).toDate
    commit2.committerEmail shouldBe "kaisellgren+foo@gmail.com"
    commit2.committerName shouldBe "Kai 2"
  }

  it should "have the message 'foo bar baz qux'" in {
    commit2.message shouldBe "foo bar baz qux"
  }

  it should "point to tree ID 'b744d5cddb5095249299d95ee531cbd990741140'" in {
    commit2.treeId shouldBe ObjectId("b744d5cddb5095249299d95ee531cbd990741140")
  }

  it should "have two parents with correct IDs." in {
    commit2.parentIds.length shouldBe 2
    commit2.parentIds should contain (ObjectId("b744d5cddb5095249299d95ee531cbd990741141"))
    commit2.parentIds should contain (ObjectId("b744d5cddb5095249299d95ee531cbd990741142"))
  }

  // Search the default repository for a commit.
  "The commit '6987b5626be09f59e84cb64c36b8e8a15a198798'" should "exist and be loaded properly" in {
    val repo = Repository.open(new File("src/test/resources/repositories/default/.git").getAbsolutePath)

    val commit = Commit.findById(ObjectId("6987b5626be09f59e84cb64c36b8e8a15a198798"))(repo)
    assert(commit.isDefined)
  }
}