
import sbt._
import Keys._


object MyBuild extends Build {

	lazy val common = Project(
											id="common",
											base=file("common")
	)

	lazy val persist = Project(
											id="persist",
											base=file("persist")
	) dependsOn(common)

	lazy val root = Project(
										id="root",
										base=file(".")
	) aggregate(common, persist) dependsOn(common, persist)
}