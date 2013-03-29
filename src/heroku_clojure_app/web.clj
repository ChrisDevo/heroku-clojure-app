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
  (json/read-str
    (slurp "public/whois.json")
    :key-fn keyword))

(defn get-ip [request]
  (or ((:headers request) "x-forwarded-for")
    (:remote-addr request)))

(defn mapify-ip [ip-string]
;  (or
    (string/split ip-string #"\."))
;    (string/split ip-string #"\:")))

(defn ip-to-long [ip-map]
  (map #(Long/parseLong %) ip-map))

(defn ip-to-decimal [ip-longs]
  (reduce (fn [ip-segment decimal-sum] (+ decimal-sum (* 256 ip-segment))) ip-longs))

(defn get-country [select-fn result-fn records]
;  (or
    (map result-fn (filter select-fn records)))
;    "no match"))

(defn ip-in-range? [ip-range decimal-ip]
  (and (<= (Long/parseLong (:decimal_lower_limit ip-range)) decimal-ip)
         (<= decimal-ip (Long/parseLong (:decimal_upper_limit ip-range)))))

(defn what-is-my-ip [request]
  (json/write-str (get-ip request)))

(defn what-is-my-decimal-ip [request]
  (json/write-str
    (ip-to-decimal
      (ip-to-long
        (mapify-ip
          (get-ip request))))))

;(find-matching #(select-within % "some-value") :column3 all-records)
(defn what-is-my-country [request]
  (json/write-str
    (get-country #(ip-in-range? %
;                                  22222222) ; hard-coded decimal ip, returns: ["China"]
                                  (ip-to-decimal
                                    (ip-to-long
                                      (mapify-ip
                                        (get-ip request)))))
      :country ip-country-table)))

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