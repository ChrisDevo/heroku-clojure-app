console.log("json_request.js is running...");

var ip_request = new XMLHttpRequest();
ip_request.open('GET', 'whatsmyip', false);
ip_request.send();

var ip;

if ((ip_request.status === 200) &&
       (ip_request.readyState === 4)) {
    console.log(ip_request);
    ip = ip_request.responseText;
}

var decimal_ip;
var d = ip.split('.');
decimal_ip = d[0]<<24|d[1]<<16|d[2]<<8|d[3];
console.log(decimal_ip);

var json_request = new XMLHttpRequest();
json_request.open('GET', 'whois.json', false);
json_request.send();

if ((json_request.status === 200) &&
    (json_request.readyState === 4)){

    var input = JSON.parse(json_request.responseText);

    var country = '';

    console.log(input.length);

    for (var i = 0; i < input.length; i++) {

        if ((decimal_ip >= input[i].decimal_lower_limit) &&
            (decimal_ip <= input[i].decimal_upper_limit)) {

            console.log(i);
            country += input[i].country;
            break;

        }

    }
}

document.writeln("Your country is " + country);

console.log("json_request.js is finished.");