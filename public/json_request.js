console.log("json_request.js is running...");

var ip_request = new XMLHttpRequest();
ip_request.open('GET', 'whatsmyip', false);
ip_request.send();

var ip;

if ((ip_request.status === 200) &&
       (ip_request.readyState === 4)) {
    ip = ip_request.responseText;
    console.log("ip: " + ip);
}

var decimal_ip;
var d = ip.split('.');
decimal_ip = d[0]<<24|d[1]<<16|d[2]<<8|d[3];
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

            console.log(i);
            country += country_table[i].country;
            break;

        }

        country = null;
        console.log("No match found");

    }
}

document.writeln("Your country is " + country);

console.log("json_request.js is finished.");