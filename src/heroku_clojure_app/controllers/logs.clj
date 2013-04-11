(ns heroku-clojure-app.controllers.logs
  (:use [compojure.core :only (defroutes GET POST)])
  (:require [clojure.string :as str]
            [ring.util.response :as ring]
            [heroku-clojure-app.views.logs :as view]
            [heroku-clojure-app.models.log :as model]))

(defn index []
  (view/index (model/all)))

(defn create [log]
  (when-not (str/blank? log)
    (model/create log))
  (ring/redirect "/"))

(defroutes routes
  (GET  "/" [] (index))
  (POST "/" [log] (create log)))