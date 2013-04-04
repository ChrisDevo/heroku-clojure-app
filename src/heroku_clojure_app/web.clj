(ns heroku-clojure-app.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site] :as handler]
            [compojure.response :as response]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]))

(defn- authenticated? [user pass]
  ;; TODO: heroku config:add REPL_USER=[...] REPL_PASSWORD=[...]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))
  
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

(defn- your-ip-is [request]
  "Takes the http request and passes it to the get-ip function. Returns a string
  representing the client's IP address."
  (get-ip request))

(defn- ip-to-vector [ip-string]
  "Takes a string representation of an IP address. Returns a vector of strings."
    (string/split ip-string #"\."))

(defn- ip-to-long [ip-vector]
  "Takes an IP address vector. Returns a vector with the values converted to
  Long"
  (map #(Long/parseLong %) ip-vector))

(defn- ip-to-decimal [ip-longs]
  "Takes an IP address vector consisting of Long values. Returns the decimal
  equivalent."
  (reduce
    (fn [ip-segment decimal-sum]
      (+ decimal-sum (* 256 ip-segment)))
      ip-longs))

(defn- get-country-from-ip [ip ip-in-range?]
  "Takes a predicate that finds a map in the ip-country-table with the IP in
  its range. Returns the value using the :country key found in the same map."
  (first
    (map :country
         (filter #(ip-in-range? % ip) ip-country-table))))

(defn- get-country-from-bank-id [request]
  "Takes an http request and extracts the value of :bank_id_number. It then
  returns an arbitrary country (as a string) from country-vat-table using the
  remainder of the bank ID number (BIN) divided by the number of EU countries
  (27).

  To be fully-functional this should match valid BINs against a current BIN
  database."
  (-> request
      :params
      :bank_id_number
      Long/parseLong
      (mod 27)
      country-vat-table
      :country
      str)
  )

(defn- get-billing-country [request]
  "Takes an http request and extracts the value of :billing_country. Returns
  the name of the country as a string."
  (-> request
    :params
    :billing_country
    str)
  )

(defn- get-vat-rate [country]
  "Takes a country name. Returns the :vat_rate value in the map with a matching
  country from country-vat-table."
  (Double/parseDouble
    (first
      (map :vat_rate
           (filter #(= (:country %) country) country-vat-table)))))

(defn- get-sales-total [request]
  "Takes an http request and extracts the value of :sales_total. Returns it as
  a Double."
  (-> request
      :params
      :sales_total
      Double/parseDouble)
  )

(defn- ip-in-range? [ip-country-map decimal-ip]
  "Takes a single map from ip-country-table and a decimal IP address. Returns
  the map that contains that decimal IP address within its range."
  (and (<= (Long/parseLong (:decimal_lower_limit ip-country-map)) decimal-ip)
    (<= decimal-ip (Long/parseLong (:decimal_upper_limit ip-country-map)))))

(defn- your-decimal-ip-is [request]
  "Takes an http request. Returns the decimal equivalent of the IP address found
  in the http header as a json string."
  (-> request
      get-ip
      ip-to-vector
      ip-to-long
      ip-to-decimal
      json/write-str))

(defn- your-country-is [request]
  "Takes an http request. Returns the :country value (as a json string) from an
  ip-country-table map that contains the IP address found in the http header."
  (-> request
      get-ip
      ip-to-vector
      ip-to-long
      ip-to-decimal
;   (-> 3261761842 ;hard-coded value for testing on localhost
      (get-country-from-ip ip-in-range?)
      str))

(defn- your-vat-rate-is [request]
  "Takes an http request. Returns the :vat_rate value (as a json string) from
  the country-vat-table map specified by the country value of the corresponding
  map."
  (-> request
      your-country-is
;   (-> 3261761842 ;hard-coded value for testing on localhost
      (get-country-from-ip ip-in-range?)
      get-vat-rate
      str))

(defn- your-total-is [request]
  "Takes an http request and returns the sales total (including appropriate
  VAT) based upon which country-choosing function is used. Which method used
  can be handled here (with another function taking all three country values)
  or on the client-side (passing a single country in the http request)."
  (-> request
;      your-country-is
;      get-billing-country;
      get-country-from-bank-id;
      get-vat-rate
      (/ 100.0)
      (* (get-sales-total request))
      str))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
      (session/wrap-session)
      (basic/wrap-basic-authentication authenticated?)))

(defroutes app-routes
  (ANY "/repl" {:as req}
       (drawbridge req))
  (GET "/" []
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (pr-str (first ip-country-table))})
  (GET "/whatsmyip" [request]
    your-ip-is)
  (GET "/whatsmydecimalip" [request]
    your-decimal-ip-is)
  (GET "/whatsmycountry" [request]
    your-country-is)
  (GET "/whatsmyvatrate" [request]
    your-vat-rate-is)
  (POST "/calculatetotal" [request]
    your-total-is)
  (route/files "/" {:root "public"})
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(def app
  (handler/site app-routes))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))
        ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
        store (cookie/cookie-store {:key (env :session-secret)})]
    (jetty/run-jetty (-> #'app
                         ((if (env :production)
                            wrap-error-page
                            trace/wrap-stacktrace))
                         (site {:session {:store store}}))
                     {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))