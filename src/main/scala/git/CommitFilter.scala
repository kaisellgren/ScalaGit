package git

class CommitFilter {
  var limit = 100
  var offset = 0
  var sort = CommitSortStrategy.Time
  var since: List[AnyRef] = _
  var until: String = _
}