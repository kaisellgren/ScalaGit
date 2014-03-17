name := "ScalaGit"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.RC2" % "test"

libraryDependencies += ("org.scala-stm" %% "scala-stm" % "0.7")

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.5"

org.scalastyle.sbt.ScalastylePlugin.Settings