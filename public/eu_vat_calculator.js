console.log('json_request.js is running...');

var ip_request = new XMLHttpRequest();
var ip;
var decimal = 0;
var ip_array;
var client_ip = document.getElementById('client_ip');
var decimal_ip = document.getElementById('decimal_ip');

ip_request.open('GET', 'whatsmyip');
ip_request.onreadystatechange = function() {

    if ((ip_request.status === 200) &&
           (ip_request.readyState === 4)) {

        ip = ip_request.responseText;
        client_ip.innerHTML = ('Your IP is: ' + ip);
    }

}
ip_request.send();

console.log('decimal_ip: ' + decimal_ip);

    var decimal_request = new XMLHttpRequest();

    decimal_request.open('GET', 'whatsmydecimalip');
    decimal_request.onreadystatechange = function() {

    if ((decimal_request.status === 200) &&
        (decimal_request.readyState === 4)) {

        console.log(decimal_request.responseText);
        decimal_ip.innerHTML = "Your decimal IP is: " +
        decimal_request.responseText;

    }
}
decimal_request.send();

var country = document.getElementById('country');
var country_request = new XMLHttpRequest();
country_request.open('GET', 'whatsmycountry');
country_request.onreadystatechange = function() {

    if ((country_request.status === 200) &&
        (country_request.readyState === 4)) {

        console.log('Response: ' + country_request.responseText);
        country.innerHTML = 'Your IP address is allocated to: '
            + country_request.responseText.replace(/\"/g, "");

    }

}
country_request.send();

var vat_rate = document.getElementById('vat_rate');
var vat_request = new XMLHttpRequest();
vat_request.open('GET', 'whatsmyvatrate');
vat_request.onreadystatechange = function() {

    if ((vat_request.status === 200) &&
        (vat_request.readyState === 4)) {

        console.log('Response: ' + vat_request.responseText);
        vat_rate.innerHTML = 'Your VAT rate is: ' +
            vat_request.responseText + ' percent';

    }

}
vat_request.send();

console.log('json_request.js is finished.');