# scala-meteor-chat

This project is an attempt to get [Atmosphere](http://atmosphere.java.net/) working with Scala and Jetty.  People are clearly managing to use Atmosphere with Scala but for some reason I’m getting nowhere fast!

So, this project is based on the [meteor-chat example](https://github.com/Atmosphere/atmosphere/tree/master/samples/meteor-chat) provided with Atmosphere.

## Changes to date

So far this example is still completely in Java.  The only changes to date are the creation of an [sbt](https://github.com/harrah/xsbt/wiki) build file `build.sbt` which uses the [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin) with it's configuration in the `project/plugins/build.sbt` file.

This allows the project to be built and run with the command:

    sbt jetty-run

Finally, I have changed line 3 of `application.js`:

    url: '/atmosphere-meteor-chat/Meteor',

to

    url: '/Meteor',
    
Which seemed to me to be a mistake - but I could easily be wrong.

## The output

When I run this, at the present time, I get this log output:

    > jetty-run
    [info] jetty-7.3.1.v20110307
    [info] NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
    [info] started o.e.j.w.WebAppContext{/,file:/Users/sroebuck/Dropbox/Projects/scala-meteor-chat/target/webapp/},/Users/sroebuck/Dropbox/Projects/scala-meteor-chat/target/webapp
    16:50:53.650 [main] INFO  org.atmosphere.cpr.AtmosphereServlet - initializing atmosphere framework: 0.8-SNAPSHOT
    16:50:53.658 [main] INFO  org.atmosphere.cpr.AtmosphereServlet - using default broadcaster class: class org.atmosphere.cpr.DefaultBroadcaster
    16:50:53.680 [main] INFO  org.atmosphere.cpr.AtmosphereServlet - Atmosphere is using comet support: org.atmosphere.container.Servlet30Support running under container: jetty/7.3.1.v20110307 using javax.servlet/3.0
    16:50:53.680 [main] INFO  org.atmosphere.cpr.AtmosphereServlet - using broadcaster class: org.atmosphere.cpr.DefaultBroadcaster
    16:50:53.684 [main] INFO  o.a.h.ReflectorServletProcessor - Installing Servlet org.atmosphere.samples.chat.MeteorChat
    16:50:53.684 [main] INFO  org.atmosphere.cpr.AtmosphereServlet - started atmosphere framework: 0.8-SNAPSHOT
    [info] Started SelectChannelConnector@0.0.0.0:8080
    > 16:51:00.394 [qtp302043267-61 - /Meteor?0] DEBUG o.a.cpr.AsynchronousProcessor - (suspend) invoked:
     HttpServletRequest: [GET /Meteor?0]@1393180668 org.eclipse.jetty.server.Request@530a3ffc
     HttpServletResponse: HTTP/1.1 200 


    16:51:00.409 [qtp302043267-61 - /Meteor?0] INFO  o.a.commons.util.EventsLogger - onSuspend(): AtmosphereResourceEventImpl{isCancelled=false, isResumedOnTimeout=false, message=null, resource=AtmosphereResourceImpl{, action=org.atmosphere.cpr.AtmosphereServlet$Action@35cd9e66, broadcaster=org.atmosphere.cpr.DefaultBroadcaster, cometSupport=org.atmosphere.container.Servlet30Support@4e5a5622, serializer=null, isInScope=true, useWriter=true, listeners=[org.atmosphere.commons.util.EventsLogger@cccfa5e]}, throwable=null}
    16:51:00.416 [qtp302043267-61 - /Meteor?0] DEBUG o.a.container.Servlet30Support - Suspending response: HTTP/1.1 200 
    Content-Type: text/html;charset=ISO-8859-1
    Set-Cookie: JSESSIONID=1qgkjq7ku165d16wro6xydooql;Path=/
    Expires: -1
    Cache-Control: no-store, no-cache, must-revalidate
    Pragma: no-cache
    Access-Control-Allow-Origin: *


    16:51:00.416 [Atmosphere-AsyncWrite-0] INFO  o.a.commons.util.EventsLogger - onBroadcast(): AtmosphereResourceEventImpl{isCancelled=false, isResumedOnTimeout=false, message=<script type='text/javascript'>
    window.parent.app.update({ name: "localhost", message: "has suspended a connection from 0:0:0:0:0:0:0:1%0" });
    </script>
    , resource=AtmosphereResourceImpl{, action=org.atmosphere.cpr.AtmosphereServlet$Action@35cd9e66, broadcaster=org.atmosphere.cpr.DefaultBroadcaster, cometSupport=org.atmosphere.container.Servlet30Support@4e5a5622, serializer=null, isInScope=true, useWriter=true, listeners=[org.atmosphere.commons.util.EventsLogger@cccfa5e]}, throwable=null}
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
    16:51:38.895 [qtp302043267-58 - /Meteor] DEBUG o.a.cpr.AsynchronousProcessor - (suspend) invoked:
     HttpServletRequest: [POST /Meteor]@1550100593 org.eclipse.jetty.server.Request@5c64a871
     HttpServletResponse: HTTP/1.1 200 


    16:51:38.898 [Atmosphere-AsyncWrite-1] INFO  o.a.commons.util.EventsLogger - onBroadcast(): AtmosphereResourceEventImpl{isCancelled=false, isResumedOnTimeout=false, message=<script type='text/javascript'>
    window.parent.app.update({ name: "System Message from localhost", message: "Stuart has joined." });
    </script>
    , resource=AtmosphereResourceImpl{, action=org.atmosphere.cpr.AtmosphereServlet$Action@35cd9e66, broadcaster=org.atmosphere.cpr.DefaultBroadcaster, cometSupport=org.atmosphere.container.Servlet30Support@4e5a5622, serializer=null, isInScope=true, useWriter=true, listeners=[org.atmosphere.commons.util.EventsLogger@cccfa5e]}, throwable=null}
    16:51:38.909 [Atmosphere-AsyncWrite-1] DEBUG o.atmosphere.cpr.DefaultBroadcaster - onException()
    org.eclipse.jetty.io.RuntimeIOException: org.eclipse.jetty.io.EofException
    	at org.eclipse.jetty.io.UncheckedPrintWriter.setError(UncheckedPrintWriter.java:107) ~[na:na]
    	at org.eclipse.jetty.io.UncheckedPrintWriter.write(UncheckedPrintWriter.java:280) ~[na:na]
    	at org.eclipse.jetty.io.UncheckedPrintWriter.write(UncheckedPrintWriter.java:295) ~[na:na]
    	at org.atmosphere.handler.AbstractReflectorAtmosphereHandler.onStateChange(AbstractReflectorAtmosphereHandler.java:112) ~[atmosphere-runtime-0.8-SNAPSHOT.jar:0.8-SNAPSHOT]
    	at org.atmosphere.cpr.DefaultBroadcaster.broadcast(DefaultBroadcaster.java:558) [atmosphere-runtime-0.8-SNAPSHOT.jar:0.8-SNAPSHOT]
    	at org.atmosphere.cpr.DefaultBroadcaster.executeAsyncWrite(DefaultBroadcaster.java:504) [atmosphere-runtime-0.8-SNAPSHOT.jar:0.8-SNAPSHOT]
    	at org.atmosphere.cpr.DefaultBroadcaster$3.run(DefaultBroadcaster.java:523) [atmosphere-runtime-0.8-SNAPSHOT.jar:0.8-SNAPSHOT]
    	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:441) [na:1.6.0_26]
    	at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303) [na:1.6.0_26]
    	at java.util.concurrent.FutureTask.run(FutureTask.java:138) [na:1.6.0_26]
    	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886) [na:1.6.0_26]
    	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908) [na:1.6.0_26]
    	at java.lang.Thread.run(Thread.java:680) [na:1.6.0_26]
    Caused by: org.eclipse.jetty.io.EofException: null
    	at org.eclipse.jetty.server.HttpOutput.write(HttpOutput.java:149) ~[na:na]
    	at org.eclipse.jetty.server.HttpOutput.write(HttpOutput.java:96) ~[na:na]
    	at java.io.ByteArrayOutputStream.writeTo(ByteArrayOutputStream.java:109) ~[na:1.6.0_26]
    	at org.eclipse.jetty.server.HttpWriter.write(HttpWriter.java:283) ~[na:na]
    	at org.eclipse.jetty.server.HttpWriter.write(HttpWriter.java:107) ~[na:na]
    	at org.eclipse.jetty.io.UncheckedPrintWriter.write(UncheckedPrintWriter.java:271) ~[na:na]
    	... 11 common frames omitted
    16:51:38.910 [Atmosphere-AsyncWrite-1] INFO  o.a.commons.util.EventsLogger - onDisconnect(): AtmosphereResourceEventImpl{isCancelled=true, isResumedOnTimeout=false, message=null, resource=AtmosphereResourceImpl{, action=org.atmosphere.cpr.AtmosphereServlet$Action@35cd9e66, broadcaster=org.atmosphere.cpr.DefaultBroadcaster, cometSupport=org.atmosphere.container.Servlet30Support@4e5a5622, serializer=null, isInScope=true, useWriter=true, listeners=[org.atmosphere.commons.util.EventsLogger@cccfa5e]}, throwable=org.eclipse.jetty.io.RuntimeIOException: org.eclipse.jetty.io.EofException}
    Exception in thread "Atmosphere-AsyncWrite-2" java.lang.IllegalStateException: No SessionManager
    	at org.eclipse.jetty.server.Request.getSession(Request.java:1107)
    	at org.eclipse.jetty.server.Request.getSession(Request.java:1097)
    	at org.atmosphere.container.Servlet30Support.action(Servlet30Support.java:168)
    	at org.atmosphere.container.Servlet30Support.action(Servlet30Support.java:63)
    	at org.atmosphere.cpr.AtmosphereResourceImpl.resume(AtmosphereResourceImpl.java:179)
    	at org.atmosphere.cpr.DefaultBroadcaster$4.run(DefaultBroadcaster.java:589)
    	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
    	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
    	at java.lang.Thread.run(Thread.java:680)
    16:51:44.577 [qtp302043267-61 - /Meteor] DEBUG o.a.cpr.AsynchronousProcessor - (suspend) invoked:
     HttpServletRequest: [POST /Meteor]@1550100593 org.eclipse.jetty.server.Request@5c64a871
     HttpServletResponse: HTTP/1.1 200 

## Help much appreciated

If you can help me know where I'm going wrong then please tweet me: @stuey.
