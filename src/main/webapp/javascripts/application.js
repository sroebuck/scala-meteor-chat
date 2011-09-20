var count = 0;
var app = {


    login: function() {
        var name = $('#login-name').val();
        if(! name.length > 0) {
            $('#system-message').css('color','red');
            $('#login-name').focus();
            return;
        }
        $('#system-message').css('color','#2d2b3d');
        $('#system-message').text(name + ':');

        $('#login-button').prop('disabled', true);
        $('#login-form').css('display', 'none');
        $('#message-form').css('display', '');

        var query = 'action=login' + '&name=' + encodeURI($('#login-name').val());
        atmoWrapper.websocketSend(this.url, query, function(response) {
            console.log("login callback called");
            $('#message').focus();
        });
    },

    post: function() {
        var message = $('#message').val();
        if(!message > 0) {
            return;
        }
        $('#message').prop('disabled', true);
        $('#post-button').prop('disabled', true);

        var query = 'action=post' + '&name=' + encodeURI($('#login-name').val()) + '&message=' + encodeURI(message);
        atmoWrapper.websocketSend(this.url, query, function(response) {
            console.log("post callback called");
            $('#message').prop('disabled', false);
            $('#post-button').prop('disabled', false);
            $('#message').focus();
            $('#message').val('');
        });
    },

    update: function(data) {
        var p = $('<p></p>');
        p.append(data);
      
        $('#display').append(p);

        $("#display").prop('scrollTop', $('#display').prop('scrollHeight'));
    },

    setupPageInteraction: function(url) {
        this.url = url
        $('#login-name').bind('keydown', function(e) {
            if (e.keyCode == 13) {
                $('#login-button').click();
            }
        });
        $('#login-button').bind('click', app.login);
        $('#message').bind('keydown', function(e) {
            if (e.shiftKey && e.keyCode == 13) {
                $('#post-button').click();
            }
        });
        $('#post-button').bind('click', app.post);
        $('#login-name').focus();
    },

    websocketCallback: function(response) {
        console.log("websocketCallback called");
        var responseText = response.responseBody;
        console.log("response = ");
        console.log(response);
        app.update(responseText);
    }

}

// This is a wrapper for the atmosphere jQuery Plugin.  The principle reason for writing this wrapper is in order to
// document the interface so that I've got more of a clue about what is going on.
var atmoWrapper = {

    // Establish a websocket connection with the host server.  Negotiate the protocol, falling back to an alternative
    // like comet if the client or server does not support websockets.
    //
    // `url` is the URL used to establish a websocket on the server.  The first stage of establishing that
    //    connection is sending a GET to that URL.
    //
    // `callback` is a function that will be called whenever a websocket message is sent to this client.  The function
    //   should take one parameter which is the `response` object.

    websocketConnect: function(url, callback) {
        $.atmosphere.subscribe(
            url,
            callback,
            $.atmosphere.request = { transport: 'polling' } )
//            $.atmosphere.request = { transport: 'websocket' } )
    },

    // Send a message over the established websocket connection or on whatever protocol has been fallen back to.
    // At present this is configured to encode the data as if it was a web form, but it appears as though this
    // configuration works for comet but not for websocket.
    //
    // `url` is the URL used to establish a websocket on the server.  The first stage of establishing that
    //    connection is sending a GET to that URL.
    //
    // `data` is the data to send over the connection.
    //
    // `callback` is a function that will be called whenever a websocket message is sent to this client.  The function
    //   should take one parameter which is the `response` object.

    websocketSend: function(url, data, callback) {
        console.log("websocketSend -> about to send: " + data);
//        console.log("Push function = ");
//        console.log($.atmosphere.response.push);
//        var ws = $.atmosphere.websocket;
//        console.log("Websocket = ");
//        console.log(ws);

        // The syntax for the push function appeared to be as commented out below from examples, but looking at the
        // code it appears to just take a single argument and obtain the data from the global variable:
        // `$.atmosphere.request.data`.  So I've changed the call to the one below that is clear and explicit in
        // what it's doing.

//        $.atmosphere.response.push(
//            url,
//            callback,
//            $.atmosphere.request = {
//                'data': data,
//                'contentType': 'application/x-www-form-urlencoded'
//            })

        $.atmosphere.request.method = 'POST';
        $.atmosphere.request.contentType = 'application/x-www-form-urlencoded';
        $.atmosphere.request.data = data;
        $.atmosphere.response.push(url);

        callback(null);
    }

}


$(function() {
    var METEOR_URL = '/Meteor';

    atmoWrapper.websocketConnect(METEOR_URL, app.websocketCallback);
    app.setupPageInteraction(METEOR_URL);
});
