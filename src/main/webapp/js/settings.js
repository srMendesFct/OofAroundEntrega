function openCity(evt, cityName) {
    // Declare all variables
    var i, tabcontent, tablinks;

    // Get all elements with class="pure-form" and hide them
    tabcontent = document.getElementsByClassName("pure-form");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    // Get all elements with class="tablinks" and remove the class "active"
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    // Show the current tab, and add an "active" class to the link that opened the tab
    document.getElementById(cityName).style.display = "block";
    evt.currentTarget.className += " active";
}

captureDataDeleteUser = function () {
    var values = {
        tokenID: localStorage.getItem('token'),
        usernameR: localStorage.getItem('username'),
        role: localStorage.getItem('role'),
        username: localStorage.getItem('username'),
    };
    $.ajax({
        type: "POST",
        url: "https://oofaround.appspot.com/rest/delete/self",
        contentType: "application/json;charset=utf-8",
        dataType: 'json', // data type        
        crossDomain: true,
        success: function (Response) {},
        error: function (Response) {
            if (Response.status == 200) {
                localStorage.clear();
                alert("Conta eliminada com sucesso.")
            } else {
                alert("Falha ao eliminar conta");
            }
        },
        data: JSON.stringify(values) // post data || get data
    });
};

captureDataGetUserInfo = function (values) {
    var values = {
        tokenID: localStorage.getItem('token'),
        role: localStorage.getItem('role'),
        username: localStorage.getItem('username'),
    };
    $.ajax({
        type: "POST",
        url: "https://oofaround.appspot.com/rest/userinfo/self",
        contentType: "application/json;charset=utf-8",
        dataType: 'json', // data type        
        crossDomain: true,
        success: function (Response) {
            localStorage.setItem('email', Response.email);
            localStorage.setItem('country', Response.country);
            localStorage.setItem('cellphone', Response.cellphone);
            if (Response.privacy == "public") {
                localStorage.setItem('privacy', "Público")
            } else {
                localStorage.setItem('privacy', "Privado");
            }
        },
        error: function (Response) {},
        data: JSON.stringify(values) // post data || get data
    });
};

captureDataChangeUserInfo = function (event) {
    var values = {};
    values["tokenID"] = localStorage.getItem('token');
    values["role"] = localStorage.getItem('role');
    values["usernameR"] = localStorage.getItem('username');
    $.each($('form[name="Alterar Dados"]').serializeArray(), function (i, field) {
        values[field.name] = field.value;
    });
    console.log(JSON.stringify(values));
    $.ajax({
        type: "POST",
        url: "https://oofaround.appspot.com/rest/userinfo/alterself",
        contentType: "application/json;charset=utf-8",
        dataType: 'json', // data type        
        crossDomain: true,
        success: function (Response) {
            captureDataGetUserInfo();
            alert("Alteração efetuada com sucesso.");
            window.location.href = "https://oofaround.appspot.com/settings.html";
        },
        error: function (Response) {
            alert("Falha ao alterar os dados.");
            window.location.href = "https://oofaround.appspot.com/settings.html";
        },
        data: JSON.stringify(values) // post data || get data
    });
    event.preventDefault();
};

window.onload = function () {
    var user = localStorage.getItem('username');
    var image = localStorage.getItem('image');
    var date = new Date();
    var token = localStorage.getItem('expiration');
    var longday = date.getTime();
    var email = localStorage.getItem('email');
    var country = localStorage.getItem('country');
    var cellphone = localStorage.getItem('cellphone');
    var privacy = localStorage.getItem('privacy');
    document.getElementById("Alterar Password").style.display = "none";
    document.getElementById("userC").value = localStorage.getItem('username');;
    document.getElementById("emailC").value = email;
    document.getElementById("countryC").value = country;
    document.getElementById("cellphoneC").value = cellphone;
    document.getElementById("privacy").value = privacy;
    document.getElementById("profilePic").src = 'data:image/jpeg;base64, ' + image;
    document.getElementById("user").innerHTML = user;
    document.getElementById("profilePicBig").src = 'data:image/jpeg;base64, ' + image;
    if (longday > token) {
        localStorage.clear();
        window.location.href = "https://oofaround.appspot.com/";
    } else {
        localStorage.setItem('expiration', date.getTime() + 300000);
        setupCallback();
    }
};

setupCallback = function () {

    var frmsl = $('form[name="Alterar Dados"]');
    /*var frms = $('form[name="Alterar Password"]');
    frms[0].onsubmit = captureDataChangePassword;*/
    frmsl[0].onsubmit = captureDataChangeUserInfo;

    document.getElementById("delete").addEventListener("click", function () {
        captureDataDeleteUser();
        window.location.href = "https://oofaround.appspot.com/";
    });

    document.getElementById("logout").addEventListener("click", function () {
        localStorage.clear();
        alert("Sessão terminada.")
        window.location.href = "https://oofaround.appspot.com/";
    });
};
