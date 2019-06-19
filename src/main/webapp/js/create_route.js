var map;
var directionsService, directionsDisplay;
var geocoder;
var presetMarkers = [];
var routePoints = [];
var waypts = [];
var locationNames = [];

//qd for necessario criar marker pelo nome
function codeAddress(addr) {
    geocoder.geocode({ address: addr}, function(results, status) {
        if(status == 'OK') {
            map.setCenter(results[0].geometry.location);
            var marker = new google.maps.Marker({ position: results[0].geometry.location, map: map});
        }
        else {
          alert('Error');
        }
    });
}

function initMap() {
    directionsService = new google.maps.DirectionsService;
    directionsDisplay = new google.maps.DirectionsRenderer;
    map = new google.maps.Map(document.getElementById('map'), {
      zoom: 9,
      center: {lat: 38.71667, lng: -9.13333}
    });
    
    geocoder = new google.maps.Geocoder();

    google.maps.event.addListener(map, 'click', function(event) {
      console.log('FUNCAO PLACEID' + getAddress(event.latLng));
      console.log('latitude do mambo ' + event.latLng.lat());
      var marker = new google.maps.Marker({
        position: event.latLng, 
        map: map,
        icon: 'https://maps.google.com/mapfiles/ms/icons/green-dot.png'
       });
       routePoints.push(marker);
       var newLoc = {
         name: "hum",
         category: "undefined",
         placeId: getPlaceId(event.latLng),
         latitude: event.latLng.lat(),
         longitude: event.latLng.lng()
       }
       locationNames.push(newLoc);
    });
    
    directionsDisplay.setMap(map);

    document.getElementById('submitF').addEventListener('click', function() {
      createWaypoints();
      calculateAndDisplayRoute(directionsService, directionsDisplay);
    });

  }

  function getAddress(location) {
    geocoder.geocode({'location': location}, function(results, status) {
      if(status == 'OK') {
        return results[0].formatted_address;
      }
      else {
        alert('Error NO CODE ADDRESS?');
      }
  });
  }

  function getPlaceId(location) {
    geocoder.geocode({'location': location}, function(results, status) {
      if(status == 'OK') {
        return results[1].place_id;
      }
      else {
        alert('Error NO PLACEID????');
      }
  });
  }

  function createWaypoints() {
    for(var i = 1; i < routePoints.length - 1; i++) {
      waypts.push({
        location: routePoints[i].position,
        stopover: true
      });
    }
  }

  function calculateAndDisplayRoute(directionsService, directionsDisplay) {
    directionsService.route({
      origin: routePoints[0].position,
      waypoints: waypts,
      destination: routePoints[routePoints.length - 1].position,
      travelMode: 'WALKING'
    }, function(response, status) {
      if (status === 'OK') {
        directionsDisplay.setDirections(response);
      } else {
        window.alert('Directions request failed due to ' + status);
      }
    });
  }

captureDataCreateCourse = function() {
  var values = {};
  values['tokenID'] = localStorage.getItem('token');
  values['usernameR'] = localStorage.getItem('username');
  values['role'] = localStorage.getItem('role');
  values['creatorUsername'] = localStorage.getItem('username');
  values['locationNames'] = locationNames;

  $.each($('form[name="courseForm"]').serializeArray(), function (i, field) {
    values[field.name] = field.value;
  });

  $.ajax({
    type: "POST",
    url: "https://oofaround.appspot.com/rest/route/create",
    contentType: "application/json;charset=utf-8",
    dataType: 'json',
    crossDomain: 'true',
    success: function(response) {
      alert('Percurso criado!');
    },
    error: function (response) {
      alert('Erro!')
    },
    data: JSON.stringify(values)
});

}

captureDataMonuments = function() {
    var values = { 
        tokenID: localStorage.getItem('token'),
        usernameR: localStorage.getItem('username'),
        role: localStorage.getItem('role'),
        limit: "",
        lastName: "",
        category: "",
        region: ""
    }    
    
    $.ajax({
        type: "POST",
        url: "https://oofaround.appspot.com/rest/location/getcategoryregion",
        contentType: "application/json;charset=utf-8",
        dataType: 'json',
        crossDomain: 'true',
        success: function(response) {
            for(i = 0; i < response.locations.length; i++) {
                var pos = new google.maps.LatLng(response.locations[i].latitude, response.locations[i].longitude);

                var marker = new google.maps.Marker({
                   position: pos, 
                   map: map
                  });

                  presetMarkers.push(marker);
                  setInfo(i, response.locations[i].name, response.locations[i].address, response.locations[i].latitude, response.locations[i].longitude);
            }
        },
        error: function (response) {},
        data: JSON.stringify(values)
    });
}

function setInfo(markerNumber, name, address, latitude, longitude) {
  var m = presetMarkers[markerNumber];

  //fazer isto mais bonito
  var contentString = '<div id="content">'+
                '<div id="siteNotice">'+
                '</div>'+
                '<h2 id="firstHeading" class="firstHeading"><b>' + name + '</b></h2>'+
                '<div id="bodyContent">'+
                '<p>Endereço: ' + address + '</p>'+
                '<p>Coordenadas: ' + latitude +  ' , ' + longitude + '</p>'+
                '</div>'+
                '</div>';

                m.addListener('click', function() {
                  var infowindow = new google.maps.InfoWindow({
                    content: contentString
                  });
                  infowindow.open(map, m);
                });

}

window.onload = function() {
  var image = localStorage.getItem('image');
  var user = localStorage.getItem('username');
  document.getElementById("profilePic").src = 'data:image/jpeg;base64, ' + image;
  document.getElementById("user").innerHTML = user;
  captureDataMonuments();
  var form_c = $('form[name="courseForm"]');
  form_c[0].onsubmit = captureDataCreateCourse;
}