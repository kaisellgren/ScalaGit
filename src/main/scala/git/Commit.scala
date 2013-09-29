package git

import java.util.{GregorianCalendar, Date}

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

  override def equals(o: Any) = o match {
    case that: Commit => that.id.sha == id.sha
    case _ => false
  }

  override def hashCode = id.sha.hashCode

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
    o.treeId = ObjectId.fromHash(new String(data.take(40).map(_.toByte)))

    data = data.drop(40 + 1) // One LF.

    // What follows is 0-n number of parent references.
    def parseParentIds() {
      // Stop if the data does not begin with "parent".
      if (new String(data.takeWhile(_ != 32).map(_.toByte)) == "parent") {
        data = data.drop(7) // Skip "parent ".

        o.parentIds ::= ObjectId.fromHash(new String(data.take(40).map(_.toByte)))

        data = data.drop(40 + 1) // One LF.

        parseParentIds()
      }
    }

    parseParentIds()

    // Two types of author fields follow next ("author" and "committer").
    def parseAuthorFields(): Tuple3[String, String, Date] = {
      // Name.
      val nameBytes = data.takeWhile(_ != '<')
      val name = new String(nameBytes.map(_.toByte)).trim

      data = data.drop(nameBytes.length + 1)

      // Email.
      val emailBytes = data.takeWhile(_ != '>')
      val email = new String(emailBytes.map(_.toByte)).trim

      data = data.drop(emailBytes.length + 2) // One '>' and one space.

      // Timestamp.
      val timestampBytes = data.takeWhile(_ != 32)

      val timestamp = new String(timestampBytes.map(_.toByte)).trim.toInt

      data = data.drop(timestampBytes.length + 1)

      // TZ offset.
      val tzBytes = data.takeWhile(_ != '\n')
      val timeZoneOffset = new String(tzBytes.map(_.toByte))

      data = data.drop(tzBytes.length + 1)

      // Date.
      val cal = new GregorianCalendar
      cal.setTimeInMillis(timestamp.toLong * 1000)
      val date = cal.getTime

      // Return a tuple containing the data.
      (name, email, date)
    }

    data = data.drop(7) // Skip the "author " data.

    val authorData = parseAuthorFields()
    o.authorName = authorData._1
    o.authorEmail = authorData._2
    o.authorDate = authorData._3

    data = data.drop(10) // Skip the "committer " data.

    val committerData = parseAuthorFields()
    o.committerName = committerData._1
    o.committerEmail = committerData._2
    o.commitDate = committerData._3

    // Finally the commit message.
    o.message = new String(data.map(_.toByte)).trim

    o
  }
}