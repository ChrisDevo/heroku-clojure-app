console.log("json_request.js is running...");

var json_request = new XMLHttpRequest();
json_request.open('GET', 'whois.json', false);
json_request.send();

if ((json_request.status === 200) &&
    (json_request.readyState === 4)){

    var input = JSON.parse(json_request.responseText);

    var ip = 3261761842;
    var country = '';

    console.log(input.length);

    for (var i = 0; i < input.length; i++) {

        if ((ip >= input[i].decimal_lower_limit) &&
            (ip <= input[i].decimal_upper_limit)) {

            console.log(i);
            country += input[i].country;
            break;

        }

    }
}

document.writeln("Your country is " + country);

console.log("json_request.js is finished.");