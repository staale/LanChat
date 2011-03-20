import sbt._

class LanChat(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {
    val scalaToolsRepository = "Scala-Tools Maven2 Repository" at "http://www.scala-tools.org/repo-releases/"
    val scalatest = "org.scalatest" % "scalatest" % "1.3"
    val scalaswing = "org.scala-lang" % "scala-swing" % "2.8.1"
}

// vim: set ts=4 sw=4 et:
