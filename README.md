```
   ,-,--.    _,.----.    ,---.                  ,---.                 _,---.   .=-.-.,--.--------.
 ,-.'-  _\ .' .' -   \ .--.'  \       _.-.    .--.'  \            _.='.'-,  \ /==/_ /==/,  -   , -\
/==/_ ,_.'/==/  ,  ,-' \==\-/\ \    .-,.'|    \==\-/\ \          /==.'-     /|==|, |\==\.-.  - ,-./
\==\  \   |==|-   |  . /==/-|_\ |  |==|, |    /==/-|_\ |        /==/ -   .-' |==|  | `--`\==\- \
 \==\ -\  |==|_   `-' \\==\,   - \ |==|- |    \==\,   - \       |==|_   /_,-.|==|- |      \==\_ \
 _\==\ ,\ |==|   _  , |/==/ -   ,| |==|, |    /==/ -   ,|       |==|  , \_.' )==| ,|      |==|- |
/==/\/ _ |\==\.       /==/-  /\ - \|==|- `-._/==/-  /\ - \      \==\-  ,    (|==|- |      |==|, |
\==\ - , / `-.`.___.-'\==\ _.\=\.-'/==/ - , ,|==\ _.\=\.-'       /==/ _  ,  //==/. /      /==/ -/
 `--`---'              `--`        `--`-----' `--`               `--`------' `--`-`       `--`--`

```
Scala Git
==

A pure Scala implementation of the Git version control.

*Note: this project is still a work-in-progress.*

## Examples

```scala
// Open a repository.
val repo = Repository.open("path/to/repo")

// Print all tags.
Tag.find(repo).foreach(println)

// Print author names of every commit.
Commit.find(repo).foreach((c: Commit) => println(c.authorName))

// Print last 10 commits.
Commit.find(CommitFilter(limit = 10))(repo).foreach(println)

// Print branch tip ids.
Branch.find(repo).foreach((b: Branch) => println(b.tip().id))

// Print repository head.
Repository.head(repo) match {
  case None => println("no HEAD on this repo")
  case Some(b) => println(b.tipId)
}

// Create a new tag.
val tag = Tag.create("something")

// What commit does the tag point to?
val commit = Tag.commit(tag)

// Delete the tag we just created.
Tag.delete(tag) // or just Tag.delete("something")
```

## License
This library is licensed under MIT.