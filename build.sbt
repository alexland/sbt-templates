


name := "etl-stream-1"

lazy val commonSettings = Seq(
	organization := "org.dougybarbo",
	version := "1.0-SNAPSHOT",
	scalaVersion := "2.11.7",
	sourceDirectory := new File(baseDirectory.value, "src"),
	assemblyJarName in assembly := "ETL.jar",
	scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
	libraryDependencies ++= Seq(
		"com.typesafe.akka"							%%	"akka-actor"								% 		"2.4.0",
		"com.typesafe.akka"							%%	"akka-cluster"							% 		"2.4.0",
		"com.typesafe.akka"							%%	"akka-slf4j"								% 		"2.4.0",
		"com.typesafe.akka"							%%	"akka-agent"								% 		"2.4.0",
		"com.typesafe.akka" 						%%	"akka-stream-experimental" 	%			"1.0",
		"net.databinder.dispatch"				%%	"dispatch-core" 						%			"0.11.2",
		"net.databinder.dispatch"				%%	"dispatch-json4s-native" 		%			"0.11.2",
		"com.fasterxml.jackson.module"	%% 	"jackson-module-scala"			%			"2.4.2",
		"org.scalaz"										%%	"scalaz-core"								%			"7.2.0-M3"
	),
	javacOptions in Compile ++= Seq("-target", "1.8", "-source", "1.8"),
	resolvers ++= Seq(
		"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
	),
	exportJars := true
)

lazy val testDependencies = Seq(
	"org.scalatest"		%% "scalatest"	% "2.2.4"		% "test"
)


// projects in this build
lazy val common = project.in(file("common"))
	.dependsOn("persist")
	.settings(commonSettings: _*)
	.settings(libraryDependencies ++= testDependencies)

lazy val persist = project.in(file("persist"))
	.settings(commonSettings: _*)

lazy val root = project.in(file("."))
	.settings(commonSettings: _*)
	.aggregate(common, persist)

