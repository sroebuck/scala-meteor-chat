name := "scala-meteor-chat"

organization := "org.atmosphere"

version := "0.7.2"

scalaVersion := "2.9.1"

// parallelExecution in Test := false

logLevel := Level.Info

traceLevel := 10

libraryDependencies ++= {
    val jettyVersion = "7.4.5.v20110725"
    val atmosphereVersion = "0.7.2"
    Seq(
        "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
        "org.eclipse.jetty" % "jetty-server" % jettyVersion % "jetty",
        "org.eclipse.jetty" % "jetty-websocket" % jettyVersion % "jetty",
        "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "jetty",
        "org.atmosphere" % "atmosphere-samples-commons" % atmosphereVersion,
        "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7",
        "ch.qos.logback" % "logback-classic" % "0.9.29"
    )
}

// Depend on atmosphere-samples-commons but exclude atmosphere-ping
ivyXML :=
  <dependencies>
    <exclude org="org.atmosphere" module="atmosphere-ping" />
  </dependencies>

resolvers ++= Seq(
  "sonatype" at "http://oss.sonatype.org/content/repositories/releases",
  "sonatypeSnapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
)

// disable updating dynamic revisions (including -SNAPSHOT versions)
offline := true

// Adds `jetty-run` command to sbt etc.
seq(webSettings :_*)
