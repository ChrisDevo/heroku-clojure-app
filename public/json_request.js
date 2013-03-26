console.log('json_request.js is running...');

var ip_request = new XMLHttpRequest();
var ip = '';
var decimal_ip = 0;
var ip_array;
var client_ip = document.getElementById('client_ip');

ip_request.open('GET', 'whatsmyip');
ip_request.onreadystatechange = function() {

    if ((ip_request.status === 200) &&
           (ip_request.readyState === 4)) {

        ip = ip_request.responseText; console.log('ip: ' + ip);
        client_ip.innerHTML = ('Your ip is: ' + ip);

        ip_array = ip.split('.'); console.log(ip_array);

        // convert each IP address segment to decimal
        // bit shift operations must be converted to 32bit unsigned using '>>> 0'
        ip_array[0] = (parseInt(ip_array[0]) << 24) >>> 0; console.log(ip_array[0]);
        ip_array[1] = (parseInt(ip_array[1]) << 16) >>> 0; console.log(ip_array[1]);
        ip_array[2] = (parseInt(ip_array[2]) << 8) >>> 0; console.log(ip_array[2]);
        ip_array[3] = (parseInt(ip_array[3])) >>> 0; console.log(ip_array[3]);

        for (var i = 0; i < ip_array.length; i++) {
            decimal_ip += ip_array[i];
        }

        console.log('decimal_ip: ' + decimal_ip);
    }

}
ip_request.send();

var json_request = new XMLHttpRequest();
var country_table;
var country = '';
var page_status = document.getElementById('page_status');

json_request.open('GET', 'whois.json');
json_request.onreadystatechange = function() {
    if ((json_request.status === 200) &&
        (json_request.readyState === 4)){

        var country_table = JSON.parse(json_request.responseText);

        for (var i = 0; i < country_table.length; i++) {

            if ((decimal_ip >= country_table[i].decimal_lower_limit) &&
                (decimal_ip <= country_table[i].decimal_upper_limit)) {

                country += country_table[i].country;
                page_status.innerHTML = ("Your country is " + country);
                break;
            }

        }

        if (country === '') {
            page_status.innerHTML = 'No country match found';
        }

        console.log('Records searched: ' + i);

    }
}
json_request.send();

console.log('json_request.js is finished.');