import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "library"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    cache,
    "postgresql" % "postgresql" % "9.1-901.jdbc4"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
	javaOptions in Test += "-Dconfig.file=conf/test.conf"
  )

}
