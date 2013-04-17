(ns heroku-clojure-app.models.log
  (:require [clojure.java.jdbc :as sql]))

(defn all []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results
      ["select * from logs order by id desc"]
      (into [] results))))

(defn create [log-entry]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/insert-values
      :logs ; table name
      [:ip_country ; table column names,
       :billing_country ;  log-entry must contain them in this order
       :bank_id_number
       :bank_country
       :sales_total
       :vat_country
       :vat_rate
       :vat_total
       :ip
       :transaction_id]
      log-entry)))