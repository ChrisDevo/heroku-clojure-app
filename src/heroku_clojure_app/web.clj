(ns heroku-clojure-app.web
  (:require [clojure.string :as str]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site] :as handler]
            [compojure.response :as response]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [heroku-clojure-app.services :as services]
            [heroku-clojure-app.controllers.logs :as logs]
            [heroku-clojure-app.views.layout :as layout]
            [ring.util.response :as ring]))

(defn- authenticated? [user pass]
  ;; TODO: heroku config:add REPL_USER=[...] REPL_PASSWORD=[...]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
    (session/wrap-session)
    (basic/wrap-basic-authentication authenticated?)))

(defroutes app-routes
  logs/routes
  (ANY "/repl" {:as req}
    (drawbridge req))
  (GET "/whatsmyip" [request] ; called by evc.js
    services/your-ip-is)
  (GET "/whatsmydecimalip" [request] ; called by evc.js
    services/your-decimal-ip-is)
  (GET "/whatsmyipcountry" [request] ; called by evc.js
    services/your-ip-country-is)
  (POST "/whatsmybillingcountry" [request] ; not called
    services/your-billing-country-is)
  (POST "/whatsmybankcountry" [request] ; called by evc.js
    services/your-bank-country-is)
  (POST "/whatsmyvatrate" [request] ; called by evc.js
    services/your-vat-rate-is)
  (POST "/calculatetotal" [request] ; called by evc.html
    services/your-total-is)
  (route/files "/" {:root "public"})
  (route/resources "/")
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