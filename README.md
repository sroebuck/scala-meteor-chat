# scala-meteor-chat

This project is an attempt to get [Atmosphere](http://atmosphere.java.net/) working with Scala and Jetty.  I attempted to get it running in place of socket-io in a substantial project but I was getting nowhere fast, so I decided to boil things down into something repeatable and shareable.

This project is based on the [meteor-chat example](https://github.com/Atmosphere/atmosphere/tree/master/samples/meteor-chat) provided with Atmosphere.

## Changes to date

So far this example is still completely in Java.  The only changes to date are the creation of an [sbt](https://github.com/harrah/xsbt/wiki) build file `build.sbt` which uses the [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin) with it's configuration in the `project/plugins/build.sbt` file.

This allows the project to be built and run with the commands:

    sbt
    > jetty-run

On top of that I have carried out some changes described below as fixes to problems I encountered.

Now I have still have a problem as there is an error message being generated although in other respects the system now appears to be working.  Here is the log message:

    21:59:12.916 [Atmosphere-AsyncWrite-0] INFO  o.a.commons.util.EventsLogger - onBroadcast(): AtmosphereResourceEventImpl{isCancelled=false, isResumedOnTimeout=false, message=<script type='text/javascript'>
    window.parent.app.update({ name: "localhost", message: "has suspended a connection from 0:0:0:0:0:0:0:1%0" });
    </script>
    , resource=AtmosphereResourceImpl{, action=org.atmosphere.cpr.AtmosphereServlet$Action@c63a8af, broadcaster=org.atmosphere.cpr.DefaultBroadcaster, cometSupport=org.atmosphere.container.Servlet30Support@62d77f83, serializer=null, isInScope=true, useWriter=true, listeners=[org.atmosphere.commons.util.EventsLogger@31f4a427]}, throwable=null}
    21:59:17.418 [qtp1750984746-56 - /Meteor] DEBUG o.a.cpr.AsynchronousProcessor - (suspend) invoked:
     HttpServletRequest: [POST /Meteor]@1478287298 org.eclipse.jetty.server.Request@581cdfc2
     HttpServletResponse: HTTP/1.1 200 

Note, in particular the `has suspended a connection from 0:0:0:0:0:0:0:1%0` error message.

## Problems along the way

Here are some problems I ran across and found fixes to.

### 404 error in the browser trying to access /atmosphere-meteor-chat/Meteor?0

I was getting browser errors trying to access `/atmosphere-meteor-chat/Meteor?0` and realised that the path in the `web.xml` file did not match the path in the `application.js` file.  So I changed line 3 of `application.js` from:

    url: '/atmosphere-meteor-chat/Meteor',

to

    url: '/Meteor',

### NoSuchMethodError for HttpServletRequest.isAsyncStarted using Jetty 7.3.1.v20110307

To start off using Jetty `7.3.1.v20110307` I was getting a no such method error for `javax.servlet.http.HttpServletRequest.isAsyncStarted`.  This error went away when I upgraded to Jetty `8.0.0.M3`.

Here was the error stacktrace:

    [warn] Error for /Meteor
    java.lang.NoSuchMethodError: javax.servlet.http.HttpServletRequest.isAsyncStarted()Z
    	at org.atmosphere.container.Servlet30Support.suspend(Servlet30Support.java:136)
    	at org.atmosphere.container.Servlet30Support.service(Servlet30Support.java:91)
    	at org.atmosphere.cpr.AtmosphereServlet.doCometSupport(AtmosphereServlet.java:1182)
    	at org.atmosphere.cpr.AtmosphereServlet.doPost(AtmosphereServlet.java:1164)
    	at org.atmosphere.cpr.AtmosphereServlet.doGet(AtmosphereServlet.java:1150)
    	at javax.servlet.http.HttpServlet.service(HttpServlet.java:707)
    	at org.eclipse.jetty.websocket.WebSocketServlet.service(WebSocketServlet.java:86)
    	at javax.servlet.http.HttpServlet.service(HttpServlet.java:820)
    	at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:534)

## Help much appreciated

If you can help speed up finding what's going wrong then please tweet me: @stuey.
