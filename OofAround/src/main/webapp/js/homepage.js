window.onload = function () {
    var frmsr = $('form[name="register"]');
    var frmsl = $('form[name="login"]');
    frmsl[0].onsubmit = captureDataL;
    frmsr[0].onsubmit = captureDataR;
};

captureDataR = function (event) {
    var values = {};
    $.each($('form[name="register"]').serializeArray(), function (i, field) {
        values[field.name] = field.value;
    });
    $.ajax({
        type: "POST",
        url: "https://oofaroundtest.appspot.com/rest/register/user",
        contentType: "application/json;charset=utf-8",
        dataType: 'json', // data type        
        crossDomain: true,
        success: function (Response) {},
        error: function (Response) {
            console.log(Response.status);
            if (Response.status == 200) {
                alert("Registo efetuado com sucesso.");
                window.location.href = "https://oofaroundtest.appspot.com/";
            } else {
                alert("Registo falhado.");
                window.location.href = "https://oofaroundtest.appspot.com/";
            }

        },
        data: JSON.stringify(values) // post data || get data
    });
    event.preventDefault();
};

captureDataL = function (event) {
    var values = {};
    $.each($('form[name="login"]').serializeArray(), function (i, field) {
        values[field.name] = field.value;
    });
    $.ajax({
        type: "POST",
        url: "https://oofaroundtest.appspot.com/rest/login/",
        contentType: "application/json;charset=utf-8",
        dataType: 'json', // data type        
        crossDomain: true,
        success: function (Response) {
            var date = new Date();
            localStorage.setItem('username', Response.username);
            localStorage.setItem('token', Response.tokenID);
            localStorage.setItem('role', Response.role);
            localStorage.setItem('expiration', date.getTime() + 300000 );
            alert("Sessão iniciada.");
            window.location.href = "https://oofaroundtest.appspot.com/homepage_logged.html";

        },
        error: function (Response) {
            alert("Falha ao iniciar sessão.");
            window.location.href = "https://oofaroundtest.appspot.com/";
        },
        data: JSON.stringify(values)
    });
    event.preventDefault();
};