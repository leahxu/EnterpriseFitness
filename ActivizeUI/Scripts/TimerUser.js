$(function() {
    refresh = function() {
        setInterval(function() {
            event();
        }, 20);
    }
});

function event() {
    $.ajax({
        url: "http://activizeweb.cloudapp.net/Home/UserInfo",
        context: document.body
    });
}