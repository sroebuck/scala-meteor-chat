name := "scala-meteor-chat"

organization := "org.atmosphere"

version := "0.7.2"

scalaVersion := "2.9.1"

// parallelExecution in Test := false

logLevel := Level.Info

traceLevel := 10

libraryDependencies ++= {
//  val jettyVersion = "7.2.2.v20101205"
//    val jettyVersion = "7.4.5.v20110725"
//    val jettyVersion = "7.5.1.v20110908"
//    val jettyVersion = "8.0.0.M3"
    val jettyVersion = "8.0.1.v20110908"
//    val atmosphereVersion = "0.7.2"
    val atmosphereVersion = "0.8-SNAPSHOT"
    Seq(
//        "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
//        "org.apache.geronimo.specs" % "geronimo-servlet_2.4_spec" % "1.1.1" % "provided",
//        "org.apache.geronimo.specs" % "geronimo-servlet_2.5_spec" % "1.2" % "provided",
//        "org.apache.geronimo.specs" % "geronimo-servlet_3.0_spec" % "1.0" % "provided",
//        "javax.servlet.jsp" % "javax.servlet.jsp-api" % "2.2.1",
        "org.eclipse.jetty" % "jetty-server" % jettyVersion % "provided,jetty",
        "org.eclipse.jetty" % "jetty-websocket" % jettyVersion % "provided,jetty",
        "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "provided,jetty",
        "org.atmosphere" % "atmosphere-samples-commons" % atmosphereVersion,
        "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7",
        "ch.qos.logback" % "logback-classic" % "0.9.29",
        "net.liftweb" %% "lift-json" % "2.4-M4"      
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
