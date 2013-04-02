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

(defn get-ip [request]
  "Takes an http request. Returns the IP address found in the header."
  (or ((:headers request) "x-forwarded-for")
    (:remote-addr request)))

(defn what-is-my-ip [request]
  "Takes the http request and passes it to the get-ip function. Returns a string
  representing the client's IP address."
  (get-ip request))

(defn ip-to-vector [ip-string]
  "Takes a string representation of an IP address. Returns a vector of strings."
    (string/split ip-string #"\."))

(defn ip-to-long [ip-vector]
  "Takes an IP address vector. Returns a vector with the values converted to
  Long"
  (map #(Long/parseLong %) ip-vector))

(defn ip-to-decimal [ip-longs]
  "Takes an IP address vector consisting of Long values. Returns the decimal
  equivalent."
  (reduce
    (fn [ip-segment decimal-sum]
      (+ decimal-sum
        (* 256 ip-segment))) ip-longs))

(defn get-country [ip-in-range? ip]
  "Takes a predicate that finds a map in the ip-country-table with the IP in
  its range. Returns the value using the :country key found in the same map."
  (map :country (filter #(ip-in-range? % ip) ip-country-table)))

(defn get-vat-rate [country]
  "Takes a country name. Returns the :vat_rate value in the map with a matching
  country from country-vat-table."
  (map :vat_rate (filter #(= (:country %) country) country-vat-table)))

(defn ip-in-range? [ip-country-map decimal-ip]
  "Takes a single map from ip-country-table and a decimal IP address. Returns
  the map that contains that decimal IP address within its range."
  (and (<= (Long/parseLong (:decimal_lower_limit ip-country-map)) decimal-ip)
    (<= decimal-ip (Long/parseLong (:decimal_upper_limit ip-country-map)))))

(defn what-is-my-decimal-ip [request]
  "Takes an http request. Returns the decimal equivalent of the IP address found
  in the http header as a json string."
  (json/write-str
    (ip-to-decimal
      (ip-to-long
        (ip-to-vector
          (get-ip request))))))

(defn what-is-my-country [request]
  "Takes an http request. Returns the :country value (as a json string) from an
  ip-country-table map that contains the IP address found in the http header."
  (json/write-str
    (first
      (get-country ip-in-range?
        (ip-to-decimal
          (ip-to-long
            (ip-to-vector
              (get-ip request))))))))

(defn what-is-my-vat-rate [request]
  "Takes an http request. Returns the :vat_rate value (as a json string) from
  the country-vat-table map specified by the country value of the corresponding
  map."
  (json/write-str
    (Long/parseLong
      (first
        (get-vat-rate
          (first
            (get-country ip-in-range?
              (ip-to-decimal
                (ip-to-long
                  (ip-to-vector
                    (get-ip request)))))))))))

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
    what-is-my-ip)
  (GET "/whatsmydecimalip" [request]
    what-is-my-decimal-ip)
  (GET "/whatsmycountry" [request]
    what-is-my-country)
  (GET "/whatsmyvatrate" [request]
    what-is-my-vat-rate)
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