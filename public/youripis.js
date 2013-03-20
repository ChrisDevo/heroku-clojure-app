console.log("'youripis.js' is running...");

function successFunction(position) {
  var lat = position.coords.latitude;
  var long = position.coords.longitude;
  console.log('Your latitude is: '+ lat +'\n and longitude is: ' + long);
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

if (request.status == 200) {
    console.log(request);
    document.writeln("Your IP address is: " + request.responseText);
}
console.log("'youripis.js' is finished.");