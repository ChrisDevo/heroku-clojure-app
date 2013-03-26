console.log("json_request.js is running...");

var ip_request = new XMLHttpRequest();
ip_request.open('GET', 'whatsmyip', false);
ip_request.send();

var ip = '';

if ((ip_request.status === 200) &&
       (ip_request.readyState === 4)) {
    ip = ip_request.responseText;
    console.log("ip: " + ip);
}

var decimal_ip = 0;
var ip_array = ip.split('.');

console.log(ip_array);

// convert each IP address segment to decimal
// bit shift operations must be converted to 32bit unsigned using '>>> 0'
ip_array[0] = (parseInt(ip_array[0]) << 24) >>> 0; console.log(ip_array[0]);
ip_array[1] = (parseInt(ip_array[1]) << 16) >>> 0; console.log(ip_array[1]);
ip_array[2] = (parseInt(ip_array[2]) << 8) >>> 0; console.log(ip_array[2]);
ip_array[3] = (parseInt(ip_array[3])) >>> 0; console.log(ip_array[3]);

for (var i = 0; i < ip_array.length; i++) {
    decimal_ip += ip_array[i];
}

console.log("decimal_ip: " + decimal_ip);

var json_request = new XMLHttpRequest();
json_request.open('GET', 'whois.json', false);
json_request.send();

if ((json_request.status === 200) &&
    (json_request.readyState === 4)){

    var country_table = JSON.parse(json_request.responseText);

    var country = '';

    for (var i = 0; i < country_table.length; i++) {

        if ((decimal_ip >= country_table[i].decimal_lower_limit) &&
            (decimal_ip <= country_table[i].decimal_upper_limit)) {

            country += country_table[i].country;
            document.writeln("Your country is " + country);
            break;
        }

    }

    if (country === '') {
        document.writeln("No country match found");
    }

    console.log("Records searched: " + i);

}

console.log("json_request.js is finished.");