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

[![Build Status](https://drone.io/github.com/kaisellgren/ScalaGit/status.png)](https://drone.io/github.com/kaisellgren/ScalaGit/latest)

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
Branch.find(repo).foreach((b: Branch) => println(Branch.tip(b)(repo).id))

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

## Contributing

* Fork and clone the repository.
* Run `sbt update`. If you use IntelliJ, you may run `sbt gen-idea` as well.
* Sources at `src/main/scala/` and tests at `src/test/scala/`.

## License
This library is licensed under MIT.
