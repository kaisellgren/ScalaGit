package git

class CommitFilter {
  var limit = 100
  var offset = 0
  //var sort = CommitSortStrategy.TIME
  var since = "HEAD"
  var until: String = _
}