package git

class apis {
  def test() {
    /*
    // API Option 1 -- immutable data, but mutable refs (only where needed)
    // Cons: Refs are not immutable
    // Pros: Simple for us and users of our lib
    val repo = Repository.open("...")
    repo.tags.length // 3
    repo.addTag("foo")
    repo.tags.length // 4

    // API Option 2 -- immutable, stores ID codes and looks up a table/hashmap instead of using refs (still the map is mutable)
    // Pros: Pretty much immutable (except for the hashmap lookup table)
    // Cons: requires wiring/setup for us
    // Const: accessing stuff slightly slower? since they do a lookup
    val repo = Repository.open("...")
    repo.tags.length // 3 // Repository.tags is a function that does a lookup to find the instance based on some ID
    repo.addTag("foo") // Replaces the tags List on the lookup table (HashMap)
    repo.tags.length // 4

    // API Option 3 -- Change the entire world. Do it cheaply, change only what has changed.
    // Cons: every 'mutating' action needs to return a new copy of the entire thing.
    // Cons: user code using 'var repo' and ugly.
    // Pros: pure immutability in our library codebase.
    var repo = Repository.open("...")
    repo.tags.length // 3
    repo = repo.addTag("foo") // New repository instance, cheaply copied, keeping the previously given path and settings
    repo.tags.length // 4 // Since points to the new repo

    // API Option 4 -- State monads?
    */
  }
}
