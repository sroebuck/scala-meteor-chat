# Atmosphere Meteor

## Mapping out transactions

### 1 - Client initiates connect

The web client initiates a connection with:

    websocketConnect: function() {
        $.atmosphere.subscribe(
            app.METEOR_URL(),
            app.websocketCallback,
            $.atmosphere.request = { transport: 'websocket' } );
    },

This appears to start by negotiating a websocket connection to the host by some means (unknown).  It then sends a `GET`
message to the client at the `METEOR_URL`: `"/Meteor"`.

#### jquery plugin detail

It sets up various default configuration options which the `request` parameter can override:

    timeout: 300000,
    method: 'GET',
    headers: {},
    contentType : '',
    cache: true,
    async: true,
    ifModified: false,
    callback: null,
    dataType: '',
    url : url,
    data : '',
    suspend : true,
    maxRequest : 60,
    maxStreamingLength : 10000000,
    lastIndex : 0,
    logLevel : 'info',
    requestCount : 0,
    fallbackTransport : 'streaming',
    transport : 'long-polling',
    webSocketImpl: null

If there is a callback provided then this recorded as the callback for the request and is added to the general
callbacks.  Not sure why there are two.

It then checks to see if there is already an active connection.  If there is, but it is of a different transport type
than required, it is disconnected.

It then tries to establish a connection on the preferred transport and falls back to the fallback transport if the
preferred transport is websocket and the websocket is deemed to be unsupported.  This fallback is nothing to do with
whether or not the connection is established, it is based purely on an assessment of the capabilities of the browser.

When the websocket is opened the `websocket.onopen` callback is called which, in turn, calls the user defined callback
with `response.state` equal to `opening`.

### 2 - Host creates Meteor and broadcasts response

The function:

    override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
      logger.info("-> doGet")
      val mym = newMeteorForRequestResponse(request, response)
      mym.broadcast("%s has suspended a connection from %s".format(request.getServerName, request.getRemoteAddr))
    }

receives this and calls:

    private def newMeteorForRequestResponse(request: HttpServletRequest, response: HttpServletResponse): MyMeteor = {
      logger.info("-> newMeteorForRequestResponse")
      val mym = MyMeteor(request, listeners = Seq(this))
      request.getSession.setAttribute("mymeteor", mym)
      response.setContentType("text/html;charset=UTF-8")
      mym.suspend()
      mym
    }

which sets up a new Meteor associated with the `request` object and suspends it.

Returning to the `doGet` method above, it then attempts to broadcast a suspension message using the meteor.

### 3 - Client receives broadcast

The client receives the broadcast in the websocketCallback:

    websocketCallback: function(response) {
        console.log("websocketCallback called");
        var responseText = response.responseBody;
        console.log("response = ");
        console.log(response);
        app.update(responseText);
    },

and the Host `onBroadcast` method is called.  This confirms that the above broadcast has been sent and should presumably
be ignored other than for logging purposes.

### 4 - Client logs in

Attempting to login results in the following code being sent with data of the form: `"action=login&name=Stuart"`:

    websocketSend: function(data, callback) {
        $.atmosphere.response.push(
            app.METEOR_URL(),
            callback,
            $.atmosphere.request = {
                'data': data,
                'contentType': 'application/x-www-form-urlencoded'
            });
    }

> Note: It appears that the contentType encoding does not impact the data if it is sent by a websocket, but _does_ if
it is sent through some other streaming mechanism.

> Next task is to confirm the above for different browsers.

