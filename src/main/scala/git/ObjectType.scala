package git

object ObjectType extends Enumeration {
  type ObjectType = Value

  // Git object types are usually referred to with their lowercase names.
  val Commit = Value("commit")
  val Tree = Value("tree")
  val Blob = Value("blob")
  val Tag = Value("tag")
  val Note = Value("note")
}