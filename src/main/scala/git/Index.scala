package git

case class IndexEntry(name: String, mode: Int, stageLevel: StageLevel.StageLevel, id: ObjectId)

case class Index() {

}

object StageLevel extends Enumeration {
  type StageLevel = Value

  val Staged = Value("staged")
  val Ancestor = Value("ancestor")
  val Ours = Value("ours")
  val Theirs = Value("theirs")
}