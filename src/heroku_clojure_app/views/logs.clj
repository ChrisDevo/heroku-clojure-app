(ns heroku-clojure-app.views.logs
  (:use [hiccup.core])
  (:require [heroku-clojure-app.views.layout :as layout]))

(defn display-logs [logs]
  [:div {:id "logs sixteen columns alpha omega"}
   (map
     (fn [log]
       [:h4 {:class "log"} (html "@ " (:created_at log) " " (:body log))])
     logs)])

(defn visitor-log [logs]
  (layout/common "VISITOR LOG"
    [:div {:class "clear"}]
    (display-logs logs)))