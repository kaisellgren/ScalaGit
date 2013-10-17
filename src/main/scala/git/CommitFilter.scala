package git

case class CommitFilter(
  limit: Int = 100,
  offset: Int = 0,
  sort: CommitSortStrategy.CommitSortStrategy = CommitSortStrategy.Time,
  since: Option[List[AnyRef]] = None,
  until: Option[List[AnyRef]] = None
)