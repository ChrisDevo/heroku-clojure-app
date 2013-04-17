(ns heroku-clojure-app.services
  (:use [compojure.core :only (defroutes GET POST)])
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.java.jdbc :as sql]
            [heroku-clojure-app.controllers.logs :as logs]
            [ring.util.response :as ring]))

(def ip-country-table
  "Reads a json file containing IP/country data and stores it as Clojure data."
  (json/read-str
    (slurp "public/whois.json")
    :key-fn keyword))

(def country-vat-table
  "Reads a json file containing country/vat data and stores it as Clojure data."
  (json/read-str
    (slurp "public/eu_vat_rates.json")
    :key-fn keyword))

(defn- get-ip [request]
  "Takes an http request. Returns the IP address found in the header."
  (or ((:headers request) "x-forwarded-for")
    (:remote-addr request)))

(defn- ip->vector [ip-string]
  "Takes a string representation of an IP address. Returns a vector of strings."
  (string/split ip-string #"\."))

(defn- ip->long [ip-vector]
  "Takes an IP address vector. Returns a vector with the values converted to
  Long"
  (map #(Long/parseLong %) ip-vector))

(defn- ip->decimal [ip-longs]
  "Takes an IP address vector consisting of Long values. Returns the decimal
  equivalent."
  (reduce
    (fn [ip-segment decimal-sum]
      (+ decimal-sum (* 256 ip-segment)))
    ip-longs))

(defn- ip-in-range? [ip-country-map decimal-ip]
  "Takes a decimal IP address. Returns the map that contains that decimal IP
  address within its range."
  (and (<= (Long/parseLong (:decimal_lower_limit ip-country-map)) decimal-ip)
    (<= decimal-ip (Long/parseLong (:decimal_upper_limit ip-country-map)))))

(defn- get-country-from-ip [ip]
  "Takes a decimal IP address and finds a map in the ip-country-table with the
  IP in its range. Returns the value using the :country key found in the same
  map."
  (first
    (map :country
      (filter #(ip-in-range? % ip) ip-country-table))))

(defn- get-param-value [request param]
  "Takes an http request and a parameter key."
  (-> request
      :params
      param))

(defn- get-bank-id [request]
  "Takes an http request and extracts the value of :bank_id_number. Returns the
bank ID as a Long."
  (-> request
      (get-param-value :bank_id_number)
      Long/parseLong))

(defn- get-country-from-bank-id [bank-id]
  "Takes an http request. Returns an arbitrary country (as a string) from
country-vat-table using the remainder of the bank ID number (BIN) divided by
the number of EU countries (27).

To be fully-functional this should match valid BINs against a current BIN
database."
  (-> bank-id
      (mod 27) ; TODO replace this with a function that returns bank country from BIN database
      country-vat-table
      :country
      str))

(defn- get-ip-country [request]
  "Takes an http request and extracts the value of :ip_country. Returns
the name of the country as a string."
  (-> request
    (get-param-value :ip_country)
    str))

(defn- get-bank-country [request]
  "Takes an http request and extracts the value of :bank_country. Returns
the name of the country as a string."
  (-> request
    (get-param-value :bank_country)
    str))

(defn- get-billing-country [request]
  "Takes an http request and extracts the value of :billing_country. Returns
the name of the country as a string."
  (-> request
    (get-param-value :billing_country)
    str))

(defn- get-sales-total [request]
  "Takes an http request and extracts the value of :sales_total. Returns it as
a Double."
  (-> request
    (get-param-value :sales_total)
    Double/parseDouble))

(defn- get-vat-country [request]
  "Takes an http request and extracts the value of :billing_country. Returns
the name of the country as a string."
  (-> request
    (get-param-value :vat_country)
    str))

(defn- get-vat-rate [request]
  "Takes an http request and extracts the value of :vat_rate. Returns
the vat rate as a string."
  (-> request
    (get-param-value :vat_rate)
    Double/parseDouble))

(defn- get-vat-total [request]
  "Takes an http request and extracts the value of :vat_total. Returns
the vat rate as a Double."
  (-> request
    (get-param-value :vat_total)
    Double/parseDouble))

(defn- random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 65 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

(defn- get-transaction-id [length]
  (random-string length))

(defn- choose-vat-country [request]
  "Takes an http request containing country parameters (:ip_country,
:billing_country, and :bank_country). Compares the country values and chooses
the applicable country for determining the VAT rate."
  (if (not= (get-ip-country request)
        (get-billing-country request))
    (get-bank-country request)
    (get-ip-country request)))

(defn- choose-vat-rate [country]
  "Takes a country name. Returns the :vat_rate value in the map with a matching
country from country-vat-table."
  (Double/parseDouble
    (first
      (map :vat_rate
        (filter #(= (:country %) country) country-vat-table)))))

(defn your-ip-is [request]
  "Takes the http request and passes it to the get-ip function. Returns a string
representing the client's IP address."
  (get-ip request))

(defn your-decimal-ip-is [request]
  "Takes an http request. Returns the decimal equivalent of the IP address found
in the http header as a string."
  (-> request
    get-ip
    ip->vector
    ip->long
    ip->decimal
    str))

(defn your-ip-country-is [request]
  "Takes an http request. Returns the :country value (as a string) from an
ip-country-table map that contains the IP address found in the http header."
  (-> request
    get-ip
    ip->vector
    ip->long
    ip->decimal
    get-country-from-ip
    str))

(defn your-billing-country-is [request]
  "Takes an http request. Returns the :billing_country value (as a string)."
  (-> request
    get-billing-country
    str))

(defn your-bank-country-is [request]
  "Takes an http request. Returns the :bank_country value (as a string)."
  (-> request
    get-bank-id
    get-country-from-bank-id
    str))

(defn your-vat-country-is [request]
  "Takes an http request. Returns the :vat_country value (as a string)."
  (-> request
    (get-param-value :vat_country)
    str))

(defn your-vat-rate-is [request]
  "Takes an http request and extracts the :vat_country value. Returns the
vat-rate from country-vat-table map specified by the country value."
  (-> request
    your-vat-country-is
    choose-vat-rate
    str))

(defn log-entry [request]
  (vector
    (get-ip-country request)
    (get-billing-country request)
    (get-bank-id request)
    (get-bank-country request)
    (get-sales-total request)
    (get-vat-country request)
    (get-vat-rate request)
    (get-vat-total request)
    (get-ip request)
    (get-transaction-id 10)))

(defn your-total-is [request]
  "Takes an http request and returns the sales total (including appropriate
VAT) based upon which country-choosing function is used. Which method used
can be handled here (with another function taking all three country values)
or on the client-side (passing a single country in the http request)."
  (format "€%.2f"
    (-> request
      choose-vat-country
      choose-vat-rate
      (/ 100.0)
      (+ 1)
      (* (get-sales-total request))))
  (logs/create (log-entry request)))

(defroutes routes
  (GET "/whatsmyip" [request] ; called by evc.js
    your-ip-is)
  (GET "/whatsmydecimalip" [request] ; called by evc.js
    your-decimal-ip-is)
  (GET "/whatsmyipcountry" [request] ; called by evc.js
    your-ip-country-is)
  (POST "/whatsmybillingcountry" [request] ; not called
    your-billing-country-is)
  (POST "/whatsmybankcountry" [request] ; called by evc.js
    your-bank-country-is)
  (POST "/whatsmyvatrate" [request] ; called by evc.js
    your-vat-rate-is)
  (POST "/calculatetotal" [request] ; called by evc.html
    your-total-is))