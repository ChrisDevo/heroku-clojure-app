(ns heroku-clojure-app.web-test
  (:use clojure.test
        midje.sweet
        heroku-clojure-app.web)
  (:require [clojure.test :refer :all]
            [heroku-clojure-app.web :refer :all]
            [clojure.tools.logging :only spy]
            [clojure.data.json :as json]))

(deftest first-test
  (is false "Tests should be written"))

(deftest what-is-my-vat-rate-test
  (fact "some test"
    (what-is-my-vat-rate ...ip...) => ...result...
    (provided
      (ip-to-vector ...ip...) => ...ip-vector...
      (ip-to-long ...ip-vector) => ...ip-long...
      (ip-to-decimal ...ip-long...) => ...ip-decimal...
      (get-country anything ...ip-decimal...) => [...country...]
      (get-vat-rate ...country...) => [...vat-rate...]
      (Long/parseLong ...vat-rate...) => ...long-vat...
      (json/write-str ...long-vat...) => ...result...
      )))

(run-tests 'heroku-clojure-app.web-test)



