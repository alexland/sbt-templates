
import sbt._
import Keys._


object MyBuild extends Build {

	lazy val root = Project(
										id="etl",
										base=file(".")
	) aggregate(persist, streamgraph) dependsOn(persist, streamgraph)

	lazy val streamgraph = Project(
											id="streamgraph",
											base=file("StreamGraph")
	) dependsOn(persist)

	lazy val persist = Project(
											id="persist",
											base=file("Persist")
	)


}