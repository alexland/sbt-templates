
name := "etl"

version in ThisBuild := "1.0-SNAPSHOT"

organization in ThisBuild := "org.dougybarbo"

scalaVersion := "2.11.7"

mainClass in assembly := Some("org.dougybarbo.App.Main")

assemblyJarName in assembly := "ETL.jar"

// custom keys for this build
val gitHeadCommitSha = taskKey[String]("determines the current git commit SHA")

val makeVersionProperties = taskKey[Seq[File]]("creates version.properties file we can find at runtime")

// common settings & definitions for this build
def ETLProject(name:String): Project = (
	Project(name, file(name))
		.settings(Defaults.itSettings: _*)
		.settings(
			libraryDependencies ++= Seq(
				"org.specs2"					%%		"specs2"										%		"1.14" 			% "test",
				"org.scalaz"					%%		"scalaz-core"								%		"7.2.0-M3",
				"com.typesafe.akka" 	%%		"akka-stream-experimental"	%		"1.0"
			),
			javacOptions in Compile ++= Seq("-target", "1.8", "-source", "1.8"),
			resolvers ++= Seq(
				"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
			),
			exportJars := true,
			assemblyMergeStrategy in assembly := {
				case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
				case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
				case "application.conf"                            => MergeStrategy.concat
				case "unwanted.txt"                                => MergeStrategy.discard
				case x                                             =>
				val oldStrategy = (assemblyMergeStrategy in assembly).value
				oldStrategy(x)
			},
			mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
				case "application.conf"                           => MergeStrategy.concat
				case "reference.conf"                             => MergeStrategy.concat
				case "META-INF/spring.tooling"                    => MergeStrategy.concat
				case "overview.html"                              => MergeStrategy.rename
				case PathList("javax", "servlet", xs @ _*)        => MergeStrategy.last
				case PathList("org", "apache", xs @ _*)           => MergeStrategy.last
				case PathList("META-INF", xs @ _*)                => MergeStrategy.discard
				case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
				case "about.html"                                 => MergeStrategy.rename
				case x                                            => old(x)
			}},
			excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
				cp filter { f =>
				(f.data.getName contains "commons-logging") ||
				(f.data.getName contains "sbt-link")
			}}
)
)

gitHeadCommitSha in ThisBuild := Process("git rev-parse HEAD").lines.head

// projects in this build
lazy val common = (
	ETLProject("common")
	settings(
		makeVersionProperties := {
			val propFile = (resourceManaged in Compile).value / "version.properties"
			val content = "version=%s" format (gitHeadCommitSha.value)
			IO.write(propFile, content)
			Seq(propFile)
		},
		resourceGenerators in Compile <+= makeVersionProperties
	)
)

val client = (
	ETLProject("apiclient")
	dependsOn(common)
	settings()
)

lazy val persist = (
	ETLProject("persist")
	dependsOn(common, client)
	settings()
)












