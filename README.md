# scala-meteor-chat

This project is an attempt to get [Atmosphere](http://atmosphere.java.net/) working with Scala and Jetty with as simple an example as possible.  This project is based on the [meteor-chat example](https://github.com/Atmosphere/atmosphere/tree/master/samples/meteor-chat) provided with Atmosphere.

The implementation has now moved to using jQuery instead of Prototype as a JavaScript backend and the latest change is
to utilise the Atmosphere jQuery plugin for client side websocket interaction and to put in place a simple Scala wrapper
around the Meteor object to simplify use.

The commit tagged `prototype.js` is a working version using the Prototype.js libraries. I then modified it to use jQuery
to cut down the dependencies, this working version was tagged `jquery.js`. Now the current version uses the Atmosphere
jQuery Plugin to enable multiple transport support.

## Changes to date

* An [sbt](https://github.com/harrah/xsbt/wiki) build file `build.sbt` has been created which uses the [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin) with it's configuration in the `project/plugins/build.sbt` file.

  This allows the project to be built and run with the commands:

    sbt
    > jetty-run

* The Java class `MeteorChat` has been converted to Scala with relatively little change in code style.

* The `application.js` file has been converted from `prototype.js` to `jquery.js`.

* The jQuery and Atmosphere jQuery plugin libraries have been added.

* A simple Scala wrapper around the Meteor class has been added.

* The `application.js` file has been converted to use the Atmosphere jQuery plugin rather than direct hard coded
jQuery ajax calls.

On top of that I have carried out some changes described below as fixes to problems I encountered.

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

However, having read more about the version of Jetty here: <http://java.dzone.com/articles/roadmap-jetty-6-jetty-7-and>
I would prefer to run under Jetty 7 which seems more aligned to likely deployment at the present time.

I tried what appeared to be the latest official release: `7.5.0.v20110901` but this failed with a different error.

I tried the latest official release from one of the maven indexes: `7.4.5.v20110725` but this failed with the same `isAsyncStarted` error which I now understand to be due to the lack of Servlet 3.0 support.  So I tried this save
version with Atmosphere `0.7.2`.  This still generated the `isAsyncStarted` error.  However, given that this was trying to use the Servlet 3.0 API I noticed that I was including:

    "org.apache.geronimo.specs" % "geronimo-servlet_3.0_spec" % "1.0",

and changed this back to:

    "org.apache.geronimo.specs" % "geronimo-servlet_2.4_spec" % "1.0",

and it worked.  Then I noticed that the `scalatra` codebase chose to include:

    "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

instead, so I tried that and, although it is the Servlet 3.x API it also works!

I also tried changing the version of Atmosphere back to `0.7.2` and found that this worked fine again.  So my conslusion
here is that it is much better to include: `javax.servlet-api 3.0.1` than `geronimo-servlet_3.0_spec 1.0`.    

### Delayed output on Webkit browsers

I encountered delayed output on Webkit based browsers which seems to have been resolved by explicitly setting the
content type of the response to:

    Content-Type: text/plain;charset=UTF-8

or

    Content-Type: text/html;charset=UTF-8

Missing the content type appears to result in a delay in display on the client if the client is running Webkit. However,
in another example this wasn't enough and further investigation found this issue raised in detail here: <http://code.google.com/p/chromium/issues/detail?id=2016>.

The first workaround is to pad the output with 256 characters of output before sending anything that matters.  This
apparently works, but doesn't seem very elegant so say the least!  Atmosphere does this automatically in some situations.

The next workaround (which I haven't tried, but is listed in the explanation above) is to set:

    Content-Type: application/octet-stream
    Transfer-Encoding: chunked

However, setting the transfer encoding to 'chunked' doesn't appear to be so easy with the Servlet API.  Apparently if
the output buffering is switched off and the size is not sent then it should default to `chunked` but I haven't been
able to find a way of making that work yet!
