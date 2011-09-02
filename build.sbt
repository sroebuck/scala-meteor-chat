name := "scala-meteor-chat"

organization := "org.atmosphere"

version := "0.8-SNAPSHOT"

scalaVersion := "2.9.1"

// parallelExecution in Test := false

logLevel := Level.Info

traceLevel := 10

libraryDependencies ++= {
    val jettyVersion = "8.0.0.M3"
    Seq(
        "org.apache.geronimo.specs" % "geronimo-servlet_3.0_spec" % "1.0",
        "org.eclipse.jetty" % "jetty-server" % jettyVersion % "jetty",
        "org.eclipse.jetty" % "jetty-websocket" % jettyVersion % "jetty",
        "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "jetty",
        "ch.qos.logback" % "logback-classic" % "0.9.29"
    )
}

// Depend on atmosphere-samples-commons but exclude atmosphere-ping
ivyXML :=
  <dependencies>
    <dependency org="org.atmosphere" name="atmosphere-samples-commons" rev="0.8-SNAPSHOT">
      <exclude module="atmosphere-ping" />
    </dependency>
  </dependencies>

resolvers ++= Seq(
  "sonatype" at "http://oss.sonatype.org/content/repositories/releases",
  "sonatypeSnapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
)

// disable updating dynamic revisions (including -SNAPSHOT versions)
offline := true

// Adds `jetty-run` command to sbt etc.
seq(webSettings :_*)