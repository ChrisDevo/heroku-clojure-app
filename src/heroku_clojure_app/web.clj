(ns heroku-clojure-app.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site] :as handler]
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
  (json/read-str
    (slurp "public/whois.json")
    :key-fn keyword))

(defn get-ip [request]
  (or ((:headers request) "x-forwarded-for")
    (:remote-addr request)))

(defn mapify-ip [ip-string]
  (string/split ip-string #"\."))

(defn ip-to-long [ip-map]
  (map #(Long/parseLong %) ip-map))

(defn ip-to-decimal [ip-bigints]
  (+
    (bit-shift-left (first ip-bigints) 24)
    (bit-shift-left (second ip-bigints) 16)
    (bit-shift-left (nth ip-bigints 2) 8)
    (last ip-bigints)))

(defn get-country [select-fn result-fn records]
  (map result-fn (filter select-fn records)))

(defn select-within [rec query]
  (and (<= (Long/parseLong (:decimal_lower_limit rec)) query)
         (<= query (Long/parseLong (:decimal_upper_limit)))))

(defn what-is-my-ip [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (get-ip request)})

(defn what-is-my-decimal-ip [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (str (ip-to-decimal (ip-to-long (mapify-ip (get-ip request)))))})

(defn what-is-my-country [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (str (get-country #(select-within % 3261761842;(ip-to-decimal (ip-to-long (mapify-ip (get-ip request))))
           ) :country ip-country-table))})

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