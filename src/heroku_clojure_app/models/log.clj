(ns heroku-clojure-app.models.log
  (:require [clojure.java.jdbc :as sql]))

(defn all []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results
      ["select * from logs order by id desc"]
      (into [] results))))

(defn create [log]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/insert-values :logs [:body] [log])))