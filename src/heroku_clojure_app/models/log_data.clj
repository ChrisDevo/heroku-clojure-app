(ns heroku-clojure-app.models.log-data
  (:require [clojure.java.jdbc :as sql]
            [heroku-clojure-app.services :as services]))

(defn all []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results
      ["select * from logs order by id desc"]
      (into [] results))))

(defn create [log]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/insert-values :logs
      [:ip_country (services/get-ip-country log)]
      [:billing_country (services/get-billing-country log)]
      [:bank_id_number (services/get-bank-id log)]
      [:bank_country (services/get-bank-country log)]
      [:sales_total (services/get-sales-total log)]
      [:vat_country (services/get-vat-country log)]
      [:vat_rate (services/get-vat-rate log)]
      [:ip (services/get-ip)])))