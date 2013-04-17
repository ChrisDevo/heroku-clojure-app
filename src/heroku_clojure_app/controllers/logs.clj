(ns heroku-clojure-app.controllers.logs
  (:use [compojure.core :only (defroutes GET POST)])
  (:require [ring.util.response :as ring]
            [heroku-clojure-app.views.logs :as view]
            [heroku-clojure-app.models.log :as model]))

(defn visitor-log []
  (view/visitor-log (model/all)))

(defn create [log]
  (when-not (empty? log)
    (model/create log))
  (ring/redirect "/"))

(defroutes routes
  (GET  "/" [] (visitor-log))
  (POST "/" [log] (create log)))