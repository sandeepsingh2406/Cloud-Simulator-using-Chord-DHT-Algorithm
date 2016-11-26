name := "CS441HW4"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in Compile := Some("chordMainMethod")

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.12"

libraryDependencies += "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.4.11"

libraryDependencies += "commons-io" % "commons-io" % "2.5"

libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5")

libraryDependencies += "org.clapper" % "grizzled-slf4j_2.11" % "1.0.2"

libraryDependencies += "org.scalatest"  %% "scalatest"   % "2.2.4" % Test //note 2.2.2 works too

