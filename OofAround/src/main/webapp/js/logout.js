var user = localStorage.getItem('username');
window.onload = function init() {
    document.getElementById("user").innerHTML=user;
    var token = localStorage.getItem('expiration');
    var date = new Date();
    var longday = date.getTime();
    if (longday > token) {
        localStorage.clear();
        window.location.href = "https://oofaroundtest.appspot.com/";
    }
    else {
        setupCallback();
    }
}

setupCallback = function () {
    document.getElementById("logout").addEventListener("click", function() {
        localStorage.clear();
        alert("Sessão terminada.")
        window.location.href ="https://oofaroundtest.appspot.com/";
    });
}