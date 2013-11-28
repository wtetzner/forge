
name := "forge"

version := "0.1.0"

scalaVersion := "2.10.2"

libraryDependencies += "org.apache.ant" % "ant" % "1.9.2"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.5"

libraryDependencies += "org.eclipse.aether" % "aether-api" % "0.9.0.M3"

libraryDependencies += "org.eclipse.aether" % "aether-util" % "0.9.0.M3"

libraryDependencies += "org.eclipse.aether" % "aether-impl" % "0.9.0.M3"

libraryDependencies += "org.eclipse.aether" % "aether-connector-basic" % "0.9.0.M3"

libraryDependencies += "org.eclipse.aether" % "aether-transport-file" % "0.9.0.M3"

libraryDependencies += "org.eclipse.aether" % "aether-transport-http" % "0.9.0.M3"

libraryDependencies += "org.scala-lang" % "scala-library" % "2.10.2"

libraryDependencies += "org.apache.maven" % "maven-aether-provider" % "3.1.0"

libraryDependencies += "org.apache.maven.wagon" % "wagon-ssh" % "1.0"

libraryDependencies += "org.eclipse.aether" % "aether-transport-wagon" % "0.9.0.M3"

libraryDependencies += "org.apache.maven" % "maven-plugin-api" % "2.0"

libraryDependencies += "org.apache.maven.plugin-tools" % "maven-plugin-annotations" % "3.2"

libraryDependencies += "it.unibo.alice.tuprolog" % "tuprolog" % "2.1.1"
