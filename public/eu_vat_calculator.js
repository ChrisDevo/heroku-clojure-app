console.log('json_request.js is running...');

//var form = document.getElementById('calculate_vat');
//var billing_country = '';
//var bank_id = 0;
//var sales_total = 0;
//var billing_country_tag = document.getElementById('billing_country');
//var bank_id_tag = document.getElementById('bank_id_number');
//var sales_total_tag = document.getElementById('sales_total');

var ip_request = new XMLHttpRequest();
var ip = '';
var client_ip_tag = document.getElementById('client_ip');

ip_request.open('GET', 'whatsmyip');
ip_request.onreadystatechange = function() {

    if ((ip_request.status === 200) &&
        (ip_request.readyState === 4)) {

        console.log(ip_request.responseText);
        ip = ip_request.responseText;
        client_ip_tag.innerHTML = ('Your IP is: ' + ip);
    }

}
ip_request.send();


var decimal_request = new XMLHttpRequest();
var decimal_ip = 0;
var decimal_ip_tag = document.getElementById('decimal_ip');

decimal_request.open('GET', 'whatsmydecimalip');
decimal_request.onreadystatechange = function() {

    if ((decimal_request.status === 200) &&
        (decimal_request.readyState === 4)) {

        console.log(decimal_request.responseText);
        decimal_ip = decimal_request.responseText;
        decimal_ip_tag.innerHTML = "Your decimal IP is: " + decimal_ip;
    }
}
decimal_request.send();


var country_request = new XMLHttpRequest();
var country_name = '';
var country_tag = document.getElementById('ip_country');
var vat_request = new XMLHttpRequest();
var vat_rate = 0;
var vat_rate_tag = document.getElementById('vat_rate');
country_request.open('GET', 'whatsmycountry');
country_request.onreadystatechange = function() {

    if ((country_request.status === 200) &&
        (country_request.readyState === 4)) {

        console.log('Response: ' + country_request.responseText);

        // strip double quotes surrounding country name
        country_name = country_request.responseText.replace(/\"/g, "");

        country_tag.innerHTML = 'Your IP address is allocated to: '
            + country_name;

        vat_request.open('GET', 'whatsmyvatrate');
            vat_request.onreadystatechange = function() {

            if ((vat_request.status === 200) &&
                (vat_request.readyState === 4)) {

                console.log('Response: ' + vat_request.responseText);
                vat_rate = vat_request.responseText;
                vat_rate_tag.innerHTML = 'Your VAT rate in ' + country_name
                    + ' is ' + vat_rate + ' percent.';
            }
        }
        vat_request.send();

    }
}
country_request.send();
console.log('json_request.js is finished.');