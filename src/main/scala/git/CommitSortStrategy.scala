package git


/**
 * Represents the chosen strategy for sorting commits.
 *
 * Topological order refers to parents-before-children.
 */
object CommitSortStrategy extends Enumeration {
  type CommitSortStrategy = Value
  val None, Topological, Time, TopologicalTime, Reverse = Value
}