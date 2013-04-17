(ns heroku-clojure-app.views.logs
  (:use [hiccup.core])
  (:require [heroku-clojure-app.views.layout :as layout]))

(defn display-logs [logs]
  [:div {:id "logs sixteen columns alpha omega"}
   (map
     (fn [log]
       [:h5 {:class "log"}
        (html
          "@ " (:created_at log) ","
          " IP country: " (:ip_country log) ","
          " Billing country: " (:billing_country log) ","
          " Bank ID number: " (:bank_id_number log) ","
          " Bank country: " (:bank_country log) ","
          " Sales total: " (:sales_total log) ","
          " VAT country: " (:vat_country log) ","
          " VAT rate: " (:vat_rate log) ","
          " VAT total: " (:vat_total log) ","
          " IP: " (:ip log) ","
          " Transaction ID: " (:transaction_id log))])
     logs)])

(defn visitor-log [logs]
  (layout/common "VISITOR LOG"
    [:div {:class "clear"}]
    (display-logs logs)))