# scala-meteor-chat

This project is now an example of [Atmosphere](http://atmosphere.java.net/) working with Scala in a relatively simple
way. This project is based on the [meteor-chat example](https://github.com/Atmosphere/atmosphere/tree/master/samples/meteor-chat) provided with Atmosphere.

Unlike the project this was based on, this project uses jQuery rather than Prototype to match the use of the atmosphere
jQuery plugin.

Along the way I had quite a few simple but frustrating problems.  These are documented a little below to help prevent
anyone else having them!

## Running the example

* Install [sbt](https://github.com/harrah/xsbt/wiki) 0.11.

Built and run with the commands:

    sbt
    > jetty-run

## Problems along the way

Here are some problems I ran across and found fixes to.

### 404 error in the browser trying to access /atmosphere-meteor-chat/Meteor?0

I was getting browser errors trying to access `/atmosphere-meteor-chat/Meteor?0` and realised that the path in the `web.xml` file did not match the path in the `application.js` file.  So I changed line 3 of `application.js` from:

    url: '/atmosphere-meteor-chat/Meteor',

to

    url: '/Meteor',
    
This was just because I was serving it as a stand-alone site which is different from the original.

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

I also tried changing the version of Atmosphere back to `0.7.2` and found that this worked fine again.

Eventually I discovered that if I set the jetty libraries as `"provided,jetty"` then they were included during the
compilation and supplied for actual web serving and this seemed to solve everything removing any need for either
`javax.servlet-api` or `geronimo-servlet_X.X_spec`.

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

The calls to Atmosphere now seem to successfully including this padding of characters so I'm not entirely sure where
I went wrong before.

### Websocket support not working in Firefox 8 and Opera 11

This stackoverflow question answers the question of which browsers support WebSockets: <http://stackoverflow.com/questions/1253683/websocket-for-html5>.

One of the links: <http://techdows.com/2010/12/turn-on-websockets-in-firefox-4.html> explains how to enable WebSocket
support in Firefox releases that have it implemented but disabled.

However, to add to the confusion the websocket object has been renamed in the later versions of Firefox so you use
`MozWebSocket` rather than `WebSocket`

I therefore modified the atmosphere.jquery.js library to check for either `WebSocket` or `MozWebSocket` before falling
back to Comet, and to use `MozWebSocket` rather than `WebSocket` to create the socket if the latter was not available.

This has now been incorporated into the latest release of the `jquery.atmosphere.js` plugin.

### Websockets don't establish or establish and immediately fail on Safari and Chrome

This problem held me back for ages and was finally resolved by the simple addition of these two parameters to the
web.xml configuration:

    <init-param>
        <param-name>org.atmosphere.useWebSocket</param-name>
        <param-value>true</param-value>
    </init-param>
    <init-param>
        <param-name>org.atmosphere.cpr.WebSocketProcessor</param-name>
        <param-value>org.atmosphere.cpr.HttpServletRequestWebSocketProcessor</param-value>
    </init-param>

### Atmosphere's websocket Meteor implementation doesn't handle www form encoding of parameters

I found that when you sent parameters www form encoded they were received and interpreted as request parameters by
the web server except when the transport was websocket.  To work around this I JSON encoded all the data for all
transmissions.
 


