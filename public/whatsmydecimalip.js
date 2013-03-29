var decimal_ip_request = new XMLHttpRequest();
var client_decimal_ip = document.getElementById('decimal_ip');

decimal_ip_request.open('GET', 'whatsmydecimalip');
decimal_ip_request.onreadystatechange = function() {

    if ((decimal_ip_request.status === 200) &&
           (decimal_ip_request.readyState === 4)) {

        var ip = decimal_ip_request.responseText;
        client_decimal_ip.innerHTML = ('Your decimal IP address is: ' + ip);

    }

}
decimal_ip_request.send();