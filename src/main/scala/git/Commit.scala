package git

import java.util.Date
import git.util.Parser._

class Commit extends Object {
  def tree: Tree = repository.database.findObjectById(treeId).asInstanceOf[Tree]

  var authorName: String = _
  var authorEmail: String = _
  var authorDate: Date = _

  var committerName: String = _
  var committerEmail: String = _
  var commitDate: Date = _

  var message: String = _

  private var treeId: ObjectId = _
  var parentIds: List[ObjectId] = Nil

  def parents: List[Commit] = {
    Nil
  }

  override def toString = s"Commit(message: $message)"
}

object Commit {
  def fromObjectFile(bytes: Array[Short]): Commit = {
    /*
      Example structure:

	    "tree" <SP> <HEX_OBJ_ID> <LF>
		  ( "parent" <SP> <HEX_OBJ_ID> <LF> )*
		  "author" <SP>
  		  <SAFE_NAME> <SP>
  		  <LT> <SAFE_EMAIL> <GT> <SP>
  		  <GIT_DATE> <LF>
  		"committer" <SP>
  		  <SAFE_NAME> <SP>
  	    <LT> <SAFE_EMAIL> <GT> <SP>
  		  <GIT_DATE> <LF>
  		<LF>
  		<DATA>
    */

    val o = new Commit

    // The object file starts with "tree ", let's skip that.
    var data = bytes.drop(5)

    // Followed by tree hash.
    o.treeId = ObjectId.fromHash(new String(data.take(40)))

    data = data.drop(40 + 1) // One LF.

    // What follows is 0-n number of parent references.
    def parseParentIds() {
      // Stop if the data does not begin with "parent".
      if (new String(data.takeWhile(_ != 32)) == "parent") {
        data = data.drop(7) // Skip "parent ".

        o.parentIds ::= ObjectId.fromHash(new String(data.take(40)))

        data = data.drop(40 + 1) // One LF.

        parseParentIds()
      }
    }

    parseParentIds()

    data = data.drop(7) // Skip the "author " data.

    val authorData = parseUserFields(data)
    o.authorName = authorData._1
    o.authorEmail = authorData._2
    o.authorDate = authorData._3
    data = authorData._4

    data = data.drop(10) // Skip the "committer " data.

    val committerData = parseUserFields(data)
    o.committerName = committerData._1
    o.committerEmail = committerData._2
    o.commitDate = committerData._3
    data = committerData._4

    // Finally the commit message.
    o.message = new String(data).trim

    o
  }
}