name := "reacjilator"
version := "0.1"
scalaVersion := "2.12.8"
//resolvers += "sonatype staging" at "https://oss.sonatype.org/content/repositories/staging/"
libraryDependencies ++= Seq(
  "com.github.seratch" % "jslack" % "1.5.6",
  "com.google.cloud" % "google-cloud-translate" % "1.72.0",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.26",
  "org.slf4j" % "slf4j-log4j12" % "1.7.26",
  "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0",
  "com.amazonaws" % "aws-java-sdk-lambda" % "1.11.546"
)
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings"
)

scalafmtOnCompile := true

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}
assemblyJarName in assembly := "reacjilator.jar"
