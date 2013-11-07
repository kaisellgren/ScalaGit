package git

object FileStatus {
  val Nonexistent = 1 << 32
  val Current = 0 // Unaltered
  val New = 1 << 0
  val Staged = 1 << 1
  val Deleted = 1 << 2
  val Renamed = 1 << 3
  val IndexTypeChange = 1 << 4 // Index type change.
  val Untracked = 1 << 7
  val Modified = 1 << 8
  val Missing = 1 << 9
  val TypeChanged = 1 << 10
  val Ignored = 1 << 14
}