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

## Examples

```scala
// Open a repository.
val repo = Repository.open("path/to/repo")

// Print commits.
repo.commits.find(new CommitFilter).foreach(println)

// Print branch tip ids.
repo.branches.foreach(println(_.tip().id))
```

## License
This library is licensed under MIT.