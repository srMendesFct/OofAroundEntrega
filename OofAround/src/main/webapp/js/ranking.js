var request = 5;
var lastRequest = 0;
var last;
window.onload = captureDataR ();

captureDataR = function (event) {
    var values = {
        tokenID: localStorage.getItem('tokenID'),
        usernameR: localStorage.getItem('username'),
        role: localStorage.getItem('role'),
        username: localStorage.getItem('username'),
        lastRequest: lastRequest,
        limit: request,
        lastUsername: last
    }
    console.log(JSON.stringify(values));

    $.ajax({
        type: "POST",
        url: "https://oofaroundtest.appspot.com/rest/list/publicranking",
        contentType: "application/json;charset=utf-8",
        dataType: 'json', // data type        
        crossDomain: true,
        success: function (Response) {},
        error: function (Response) {
            console.log(Response.status);
            if (Response.status == 200) {
                listDiv = document.getElementById('ranking');
                var ul = document.createElement('ul');
                for (var i = 0; i < data.length; ++i) {
                    var li = document.createElement('li');
                    li.innerHTML = data[i];
                    ul.appendChild(li);
                }
                listDiv.appendChild(ul);
                last = data[request -1];
                lastRequest = lastRequest + request;
            }

        },
        data: JSON.stringify(values) // post data || get data
    });
    event.preventDefault();
};
