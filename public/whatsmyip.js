var ip_request = new XMLHttpRequest();
var client_ip = document.getElementById('client_ip');

ip_request.open('GET', 'whatsmyip');
ip_request.onreadystatechange = function() {

    if ((ip_request.status === 200) &&
           (ip_request.readyState === 4)) {

        var ip = ip_request.responseText;
        client_ip.innerHTML = ('Your IP address is: ' + ip);

    }

}
ip_request.send();