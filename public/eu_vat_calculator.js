console.log('json_request.js is running...');

// ip_request variables
var ip_request = new XMLHttpRequest();
var ip = '';
var client_ip_field = document.getElementById('client_ip');

// get client's IP address from http header and set html value
ip_request.open('GET', 'whatsmyip');
ip_request.onreadystatechange = function() {

    if ((ip_request.status === 200) &&
        (ip_request.readyState === 4)) {

        console.log(ip_request.responseText);
        ip = ip_request.responseText;
        client_ip_field.innerHTML = ('Your IP is: ' + ip);
    }

};
ip_request.send();
// end get client's IP address...


// decimal_request variables
var decimal_request = new XMLHttpRequest();
var decimal_ip = 0;
var decimal_ip_field = document.getElementById('decimal_ip');

// get client's decimal IP address from http header and set html value
decimal_request.open('GET', 'whatsmydecimalip');
decimal_request.onreadystatechange = function() {

    if ((decimal_request.status === 200) &&
        (decimal_request.readyState === 4)) {

        console.log(decimal_request.responseText);
        decimal_ip = decimal_request.responseText;
        decimal_ip_field.innerHTML = "Your decimal IP is: " + decimal_ip;
    }
};
decimal_request.send();
// end get client's decimal IP address...


// bank_id_request variables
var bank_id_request = new XMLHttpRequest();
var bank_id = 0;
var bank_id_field = document.getElementById('bank_id_number');
var bank_country = '';
var bank_country_submitted = document.getElementById('bank_country');
var bank_country_displayed = document.getElementById('bank_id_country');

function validateBankID() {

    bank_id = document.vat_form.bank_id_number.value;

    if (bank_id > 0) {
        bank_id_request.open('POST', 'whatsmybankcountry?bank_id_number='
        + bank_id, false);

        bank_id_request.onreadystatechange = function() {

              console.log('Bank ID: ' + bank_id);

              if ((ip_country_request.status === 200) &&
                  (ip_country_request.readyState === 4)) {

                  bank_country = bank_id_request.responseText;
                  console.log('Bank country: ' + bank_country);
                  bank_country_submitted.value = bank_country;
                  bank_country_displayed.innerHTML = 'Your bank country is: '
                      + bank_country;

              }
        };
        bank_id_request.send();
    }



}

// ip_country_request variables
var ip_country_request = new XMLHttpRequest();
var ip_country = '';
var ip_country_field = document.getElementById('ip_country');
var submitted_ip_country = document.getElementById('submitted_ip_country');

// get client's IP address country using decimal IP
ip_country_request.open('GET', 'whatsmyipcountry');
ip_country_request.onreadystatechange = function() {

    if ((ip_country_request.status === 200) &&
        (ip_country_request.readyState === 4)) {

        ip_country = ip_country_request.responseText;
        console.log('Response: ' + ip_country);

        submitted_ip_country.value = ip_country;

        ip_country_field.innerHTML = 'Your IP address is allocated to: '
            + ip_country;

    }
};
ip_country_request.send();
// end get client's IP address country using decimal IP

// vat_request variables
var vat_request = new XMLHttpRequest();
var vat_rate = 0;
var vat_rate_displayed = document.getElementById('vat_rate_displayed');
var vat_rate_submitted = document.getElementById('vat_rate');
var vat_query = 'whatsmyvatrate?vat_country=';
var vat_multiplier = 0;

function request_vat(country) {

        // get vate rate using chosen country
        vat_request.open('POST', vat_query + country, false);
        vat_request.setRequestHeader("Content-type", "text/plain")
        vat_request.onreadystatechange = function() {

            if ((vat_request.status === 200) &&
                (vat_request.readyState === 4)) {

                console.log('Response: ' + vat_request.responseText);
                vat_rate = vat_request.responseText;
                vat_rate_displayed.innerHTML = 'Your VAT rate in ' + country
                    + ' is ' + vat_rate + ' percent.';
                vat_rate_submitted.value = vat_rate;
                vat_multiplier = (vat_rate / 100) + 1;
            }
        };
        vat_request.send();
        // end get vate rate using chosen country
}

// billing_country variables
var billing_country = '';
var billing_country_field = document.getElementById('billing_country');
// sales_total variables
var sales_total = 0;
var sales_total_field = document.getElementById('sales_total');
// vat_total variables
var vat_total = 0;
var vat_total_displayed = document.getElementById('vat_total_displayed');
var vat_total_submitted = document.getElementById('vat_total');
var vat_country = '';

function calculateTotal() {

    billing_country = vat_form.billing_country.value;

    console.log('IP country: ' + ip_country);
    console.log('Billing country: ' + billing_country);
    console.log('Bank country: ' + bank_country);

    if (ip_country != billing_country) {
        vat_country = bank_country;
    } else {
        vat_country = ip_country;
    }

    console.log('VAT country: ' + vat_country);
    request_vat(vat_country);

    console.log('VAT rate: ' + vat_rate);
    console.log('VAT rate multiplier: ' + vat_multiplier);

    sales_total = vat_form.sales_total.value;
    vat_total = (sales_total * vat_multiplier).toFixed(2);
    vat_total_displayed.innerHTML = ('Your total (including VAT @ ' + vat_rate +
        ' percent) is: €' + vat_total);
    vat_total_submitted.value = vat_total;
}

console.log('json_request.js is finished.');