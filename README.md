```
                               |             _     _       |                           .      .
     >X<          ,,,,,        |.===.      o' \,=./ `o     |.===.         -*~*-      .  .:::.         ()_()
    (o o)        /(o o)\       {}o o{}        (o o)        {}o o{}        (o o)        :(o o):  .     (o o)
ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--(_)--Ooo-ooO--`o'--Ooo-

        ,-,--.    _,.----.    ,---.                  ,---.                  _,---.   .=-.-.,--.--------.
      ,-.'-  _\ .' .' -   \ .--.'  \       _.-.    .--.'  \             _.='.'-,  \ /==/_ /==/,  -   , -\
     /==/_ ,_.'/==/  ,  ,-' \==\-/\ \    .-,.'|    \==\-/\ \           /==.'-     /|==|, |\==\.-.  - ,-./
     \==\  \   |==|-   |  . /==/-|_\ |  |==|, |    /==/-|_\ |         /==/ -   .-' |==|  | `--`\==\- \
      \==\ -\  |==|_   `-' \\==\,   - \ |==|- |    \==\,   - \        |==|_   /_,-.|==|- |      \==\_ \
      _\==\ ,\ |==|   _  , |/==/ -   ,| |==|, |    /==/ -   ,|        |==|  , \_.' )==| ,|      |==|- |
     /==/\/ _ |\==\.       /==/-  /\ - \|==|- `-._/==/-  /\ - \       \==\-  ,    (|==|- |      |==|, |
     \==\ - , / `-.`.___.-'\==\ _.\=\.-'/==/ - , ,|==\ _.\=\.-'        /==/ _  ,  //==/. /      /==/ -/
      `--`---'              `--`        `--`-----' `--`                `--`------' `--`-`       `--`--`
```
Scala Git
==

A pure Scala implementation of the Git version control.

## Examples

```scala
// Open a repository.
val repo = Repository.open("path/to/repo")

// Print commits.
repo.commits.find(new CommitFilter).foreach(println)

// Print branch tip ids.
repo.branches.foreach(println(_.tip().id))

// Remove the head from the database.
repo.database -= repo.head().tip()
```

## License
This library is licensed under MIT.