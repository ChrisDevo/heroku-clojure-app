(ns heroku-clojure-app.views.logs-view
  (:use [hiccup.core :only (h)])
  (:require [heroku-clojure-app.views.layout :as layout]))

(defn display-logs [logs]
  [:div {:id "logs sixteen columns alpha omega"}
   (map
     (fn [log] [:h2 {:class "log"} (h (:body log))])
     logs)])

(defn visitor-log [logs]
  (layout/common "VISITOR LOG"
    [:div {:class "clear"}]
    (display-logs logs)))