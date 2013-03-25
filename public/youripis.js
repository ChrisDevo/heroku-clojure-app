console.log("'youripis.js' is running...");

function successFunction(position) {
  var lat = position.coords.latitude;
  var long = position.coords.longitude;
//  document.writeln('Your latitude: '+ lat +'\nlongitude: ' + long);
}

function errorFunction(position) {
  alert('Error!');
}

if ("geolocation" in navigator) {
  console.log("geolocation is available");
  navigator.geolocation.getCurrentPosition(successFunction, errorFunction);
} else {
  alert("I'm sorry, but geolocation services are not supported by your browser.");
}

var request = new XMLHttpRequest();
request.open('GET', 'whatsmyip', false);
request.send();

var ip = request.responseText;

if (request.status == 200) {
    console.log(request);
    document.writeln("Your IP address: " + ip);
}

console.log("'youripis.js' is finished.");