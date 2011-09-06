var count = 0;
var app = {
    url: '/Meteor',
    initialize: function() {
        $('#login-name').focus();
        app.listen();
    },
    listen: function() {
        $('#comet-frame').attr('src', app.url + '?' + count);
        count ++;
    },
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
        $.ajax({
            url: app.url,
            type: 'post',
            data: query,
            complete: function() {
                console.log("success");
                $('#message').focus();
            }
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
        $.ajax({
            url: app.url,
            type: 'post',
            data: query,
            complete: function() {
                console.log("success");
                $('#message').prop('disabled', false);
                $('#post-button').prop('disabled', false);
                $('#message').focus();
                $('#message').val('');
            }
        });
    },
    update: function(data) {
        var p = $('<p></p>');
        p.append(data.name + ':<br/>' + data.message);
      
        $('#display').append(p);

        $("#display").prop('scrollTop', $('#display').prop('scrollHeight'));
    }
};

$(function() {
    app.initialize();

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
});
